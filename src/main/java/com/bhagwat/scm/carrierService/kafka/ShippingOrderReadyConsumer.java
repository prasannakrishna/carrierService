package com.bhagwat.scm.carrierService.kafka;

import com.bhagwat.scm.carrierService.dto.CbrRequest;
import com.bhagwat.scm.carrierService.dto.CbrResponse;
import com.bhagwat.scm.carrierService.dto.LocationAddressDto;
import com.bhagwat.scm.carrierService.entity.*;
import com.bhagwat.scm.carrierService.enums.*;
import com.bhagwat.scm.carrierService.repository.*;
import com.bhagwat.scm.carrierService.service.CarrierBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Consumes shipping-order.ready events from whichever service originated
 * them — sellerService, wmsService, or storeService. Creates a
 * ReadyToShipOrder plus a CarrierBookingRequest and publishes an RTS event
 * for transportPlanner once a carrier is actually selected, identically
 * regardless of origin.
 *
 * Flow: seller/warehouse/store marks something READY → this consumer →
 * RTS + CBR created (DRAFT, not yet broadcast) → an operator broadcasts the
 * CBR to eligible carriers via CarrierBookingController → carriers respond →
 * shipper accepts via SellerCarrierSelectionController, which delegates to
 * CarrierBookingService.acceptResponse() — the single place a
 * TransportRequest gets created, whether the booking started here or via
 * the formal CBR REST API directly. That convergence is what removed the
 * previous eager, disconnected TransportRequest creation that used to live
 * in this consumer and could race with the formal flow's own TR creation.
 *
 * requestedByPartyType/requestedByPartyId identify the origin. Falls back to
 * SELLER + the legacy "sellerId" key for events published before these two
 * fields existed (sellerService's own producer still only sets "sellerId").
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ShippingOrderReadyConsumer {

    private final ReadyToShipOrderRepository rtsRepo;
    private final ReadyToShipItemRepository rtsItemRepo;
    private final CarrierBookingService bookingService;

    @KafkaListener(topics = "transport.shipping-order.ready", groupId = "carrier-service")
    @Transactional
    public void onShippingOrderReady(Map<String, Object> event) {
        try {
            String soId = (String) event.get("soId");
            String requestedByPartyId = (String) event.get("requestedByPartyId");
            if (requestedByPartyId == null) requestedByPartyId = (String) event.get("sellerId");
            String requestedByPartyType = (String) event.getOrDefault("requestedByPartyType", "SELLER");
            String sourceId = (String) event.get("sourceId");
            String destName = (String) event.get("destinationName");
            String destAddress = (String) event.get("destinationAddress");
            String expectedDateStr = (String) event.get("expectedDate");
            String type = (String) event.get("type");

            if (soId == null || requestedByPartyId == null) {
                log.warn("Ignoring shipping-order.ready with missing soId/requestedByPartyId");
                return;
            }

            log.info("Processing shipping-order.ready: soId={} requestedBy={}:{}", soId, requestedByPartyType, requestedByPartyId);

            ShipmentType shipmentType = mapShipmentType(type);
            LocationAddress origin = LocationAddress.builder().locationId(sourceId).city(sourceId).build();
            LocationAddress destination = LocationAddress.builder()
                    .locationId(destName).street(destAddress).city(destName).build();
            LocalDate deliveryDate = expectedDateStr != null ? LocalDate.parse(expectedDateStr) : null;

            // 1. Create ReadyToShipOrder — no carrier chosen yet
            ReadyToShipOrder rts = ReadyToShipOrder.builder()
                    .rtsNumber("RTS-" + soId.substring(0, 8).toUpperCase())
                    .soId(soId)
                    .shipmentType(shipmentType)
                    .shipper(ShipmentParty.builder()
                            .partyId(requestedByPartyId)
                            .partyName(requestedByPartyId)
                            .partyType(requestedByPartyType)
                            .build())
                    .originAddress(origin)
                    .destinationAddress(destination)
                    .cargoReadyDateTime(LocalDateTime.now())
                    .loadType(LoadType.LTL)
                    .status(RtsStatus.READY)
                    .build();
            rts = rtsRepo.save(rts);

            // Save RTS items and calculate volumetrics from SKU dimensions
            BigDecimal totalWeightKg = BigDecimal.ZERO;
            BigDecimal totalVolumeM3 = BigDecimal.ZERO;
            int totalPackages = 0;

            List<?> items = (List<?>) event.get("items");
            if (items != null) {
                for (Object item : items) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> itemMap = (Map<String, Object>) item;
                    BigDecimal qty = new BigDecimal(itemMap.getOrDefault("quantity", "0").toString());

                    ReadyToShipItem rtsItem = ReadyToShipItem.builder()
                            .rtsId(rts.getRtsId())
                            .skuId((String) itemMap.get("skuId"))
                            .productName((String) itemMap.get("productName"))
                            .quantity(qty)
                            .build();
                    rtsItemRepo.save(rtsItem);

                    // Calculate volumetrics from SKU dimensions × quantity
                    double unitLength = toDouble(itemMap.get("unitLengthCm"));
                    double unitWidth = toDouble(itemMap.get("unitWidthCm"));
                    double unitHeight = toDouble(itemMap.get("unitHeightCm"));
                    double unitWeight = toDouble(itemMap.get("unitWeightKg"));

                    if (unitWeight > 0) {
                        totalWeightKg = totalWeightKg.add(qty.multiply(BigDecimal.valueOf(unitWeight)));
                    }
                    if (unitLength > 0 && unitWidth > 0 && unitHeight > 0) {
                        // Volume per unit in m³ = (L×W×H) / 1,000,000 (cm³ to m³)
                        double unitVolM3 = (unitLength * unitWidth * unitHeight) / 1_000_000.0;
                        totalVolumeM3 = totalVolumeM3.add(qty.multiply(BigDecimal.valueOf(unitVolM3)));
                    }
                    totalPackages += qty.intValue();
                }
            }

            if (totalWeightKg.compareTo(BigDecimal.ZERO) > 0) {
                rts.setTotalWeightKg(totalWeightKg);
                rts.setTotalVolumeM3(totalVolumeM3);
                rts.setTotalPackages(totalPackages);
            }

            // 2. Create the Carrier Booking Request — the same entity the formal
            // /api/v1/carrier/bookings API creates. Left DRAFT: broadcasting to
            // specific carriers and the shipper's eventual selection both go
            // through the existing CBR machinery from here on, instead of a
            // separate ad-hoc path.
            CbrResponse cbr = bookingService.createCbr(CbrRequest.builder()
                    .requestedByPartyId(requestedByPartyId)
                    .requestedByPartyType(requestedByPartyType)
                    .requestedByPartyName(requestedByPartyId)
                    .shipmentType(shipmentType)
                    .originAddress(toAddrDto(origin))
                    .destinationAddress(toAddrDto(destination))
                    .requestedDeliveryDate(deliveryDate)
                    .loadType(LoadType.LTL)
                    .totalWeightKg(rts.getTotalWeightKg())
                    .totalVolumeM3(rts.getTotalVolumeM3())
                    .totalPackages(rts.getTotalPackages())
                    .build());

            rts.setCbrId(cbr.getCbrId());
            rtsRepo.save(rts);

            log.info("Created RTS={} and CBR={} for shipping order {}. Awaiting broadcast/carrier selection.",
                    rts.getRtsNumber(), cbr.getCbrNumber(), soId);

        } catch (Exception e) {
            log.error("Error processing shipping-order.ready: {}", e.getMessage(), e);
        }
    }

    private LocationAddressDto toAddrDto(LocationAddress a) {
        if (a == null) return null;
        return LocationAddressDto.builder()
                .locationId(a.getLocationId()).street(a.getStreet())
                .city(a.getCity()).state(a.getState())
                .pincode(a.getPincode()).country(a.getCountry())
                .build();
    }

    private ShipmentType mapShipmentType(String type) {
        if (type == null) return ShipmentType.ORDER_TO_STORE;
        return switch (type) {
            case "ORDER_TO_SITE" -> ShipmentType.ORDER_TO_WAREHOUSE;
            case "ORDER_TO_STORE" -> ShipmentType.ORDER_TO_STORE;
            case "COMMUNITY_ORDER" -> ShipmentType.ORDER_TO_CUSTOMER;
            default -> ShipmentType.ORDER_TO_STORE;
        };
    }

    private double toDouble(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0; }
    }
}
