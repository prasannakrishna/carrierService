package com.bhagwat.scm.carrierService.kafka;

import com.bhagwat.scm.carrierService.entity.*;
import com.bhagwat.scm.carrierService.enums.*;
import com.bhagwat.scm.carrierService.repository.*;
import com.bhagwat.scm.carrierService.service.VolumetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Consumes shipping-order.ready events from whichever service originated
 * them — sellerService, wmsService, or storeService. Creates
 * TransportRequest → ReadyToShipOrder → publishes RTS event for
 * transportPlanner, identically regardless of origin.
 *
 * Flow: seller/warehouse/store marks something READY → this consumer →
 * TR + RTS created → Kafka → transportPlanner auto-plans
 *
 * requestedByPartyType/requestedByPartyId identify the origin. Falls back to
 * SELLER + the legacy "sellerId" key for events published before these two
 * fields existed (sellerService's own producer still only sets "sellerId").
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ShippingOrderReadyConsumer {

    private final TransportRequestRepository trRepo;
    private final TransportRequestItemRepository trItemRepo;
    private final ReadyToShipOrderRepository rtsRepo;
    private final ReadyToShipItemRepository rtsItemRepo;
    private final CarrierKafkaProducer kafkaProducer;
    private final VolumetricService volumetricService;

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

            // 1. Create TransportRequest
            TransportRequest tr = TransportRequest.builder()
                    .trNumber("TR-" + soId.substring(0, 8).toUpperCase())
                    .shippingOrderId(soId)
                    .shippingOrderNumber(soId)
                    .requestedByPartyId(requestedByPartyId)
                    .requestedByPartyType(requestedByPartyType)
                    .requestedByPartyName(requestedByPartyId)
                    .shipmentType(mapShipmentType(type))
                    .originAddress(LocationAddress.builder()
                            .locationId(sourceId)
                            .city(sourceId)
                            .build())
                    .destinationAddress(LocationAddress.builder()
                            .locationId(destName)
                            .street(destAddress)
                            .city(destName)
                            .build())
                    .requestedDeliveryDate(expectedDateStr != null ? LocalDate.parse(expectedDateStr) : null)
                    .loadType(LoadType.LTL)
                    .status(TransportRequestStatus.PENDING)
                    .build();
            tr = trRepo.save(tr);

            // Save items
            List<?> items = (List<?>) event.get("items");
            if (items != null) {
                for (Object item : items) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> itemMap = (Map<String, Object>) item;
                    TransportRequestItem tri = TransportRequestItem.builder()
                            .trId(tr.getTrId())
                            .skuId((String) itemMap.get("skuId"))
                            .productName((String) itemMap.get("productName"))
                            .quantity(new BigDecimal(itemMap.getOrDefault("quantity", "0").toString()))
                            .build();
                    trItemRepo.save(tri);
                }
            }

            // 2. Create ReadyToShipOrder from TR
            ReadyToShipOrder rts = ReadyToShipOrder.builder()
                    .rtsNumber("RTS-" + soId.substring(0, 8).toUpperCase())
                    .trId(tr.getTrId())
                    .shipmentType(tr.getShipmentType())
                    .shipper(ShipmentParty.builder()
                            .partyId(requestedByPartyId)
                            .partyName(requestedByPartyId)
                            .partyType(requestedByPartyType)
                            .build())
                    .originAddress(tr.getOriginAddress())
                    .destinationAddress(tr.getDestinationAddress())
                    .cargoReadyDateTime(LocalDateTime.now())
                    .loadType(LoadType.LTL)
                    .status(RtsStatus.READY)
                    .build();
            rts = rtsRepo.save(rts);

            // Save RTS items and calculate volumetrics from SKU dimensions
            BigDecimal totalWeightKg = BigDecimal.ZERO;
            BigDecimal totalVolumeM3 = BigDecimal.ZERO;
            int totalPackages = 0;

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

            // Update RTS with calculated volumetrics
            if (totalWeightKg.compareTo(BigDecimal.ZERO) > 0) {
                rts.setTotalWeightKg(totalWeightKg);
                rts.setTotalVolumeM3(totalVolumeM3);
                rts.setTotalPackages(totalPackages);
                rtsRepo.save(rts);

                // Also update TR
                tr.setTotalWeightKg(totalWeightKg);
                tr.setTotalVolumeM3(totalVolumeM3);
                tr.setTotalPackages(totalPackages);
                trRepo.save(tr);
            }

            // 3. Broadcast CBR to eligible carriers (requester will choose from responses)
            kafkaProducer.publishCbrBroadcast(rts.getRtsId(), Map.of(
                    "rtsId", rts.getRtsId(),
                    "rtsNumber", rts.getRtsNumber(),
                    "trId", tr.getTrId(),
                    "requestedByPartyType", requestedByPartyType,
                    "requestedByPartyId", requestedByPartyId,
                    "soId", soId,
                    "originCity", sourceId != null ? sourceId : "",
                    "destinationCity", destName != null ? destName : "",
                    "status", "AWAITING_CARRIER_SELECTION"
            ));

            log.info("Created TR={} RTS={} for shipping order {}. Awaiting carrier selection by {}.",
                    tr.getTrNumber(), rts.getRtsNumber(), soId, requestedByPartyType);

        } catch (Exception e) {
            log.error("Error processing shipping-order.ready: {}", e.getMessage(), e);
        }
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
