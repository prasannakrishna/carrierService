package com.bhagwat.scm.carrierService.service;

import com.bhagwat.scm.carrierService.dto.*;
import com.bhagwat.scm.carrierService.entity.*;
import com.bhagwat.scm.carrierService.enums.*;
import com.bhagwat.scm.carrierService.kafka.CarrierKafkaProducer;
import com.bhagwat.scm.carrierService.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Slf4j
public class TransportRequestService {

    private final TransportRequestRepository trRepo;
    private final TransportRequestItemRepository itemRepo;
    private final ReadyToShipOrderRepository rtsRepo;
    private final ReadyToShipItemRepository rtsItemRepo;
    private final AdvancedShipmentNoticeRepository asnRepo;
    private final TransportShipmentRepository tsRepo;
    private final CarrierKafkaProducer producer;

    private String rtsNumber() { return String.format("RTS-%d-%05d", Year.now().getValue(), rtsRepo.count() + 1); }
    private String asnNumber() { return String.format("ASN-%d-%05d", Year.now().getValue(), asnRepo.count() + 1); }
    private String tsNumber()  { return String.format("TS-%d-%05d",  Year.now().getValue(), tsRepo.count() + 1); }

    @Transactional(readOnly = true)
    public TransportRequestResponse getTransportRequest(String trId) {
        TransportRequest tr = findTr(trId);
        List<TransportRequestItemDto> items = itemRepo.findByTrId(trId).stream()
                .map(this::toItemDto).collect(Collectors.toList());
        return toTrResponse(tr, items);
    }

    @Transactional
    public TransportRequestResponse addItems(String trId, List<TransportRequestItemDto> dtos) {
        findTr(trId);
        List<TransportRequestItem> items = dtos.stream().map(d -> TransportRequestItem.builder()
                .trId(trId).productId(d.getProductId()).productName(d.getProductName())
                .skuId(d.getSkuId()).variantName(d.getVariantName())
                .orderedQuantity(d.getOrderedQuantity()).packedQuantity(d.getPackedQuantity())
                .packageQuantity(d.getPackageQuantity())
                .weightKgPerUnit(d.getWeightKgPerUnit()).volumeM3PerUnit(d.getVolumeM3PerUnit())
                .isHazardous(Boolean.TRUE.equals(d.getIsHazardous()))
                .orderNumber(d.getOrderNumber()).orderLineId(d.getOrderLineId()).build()
        ).collect(Collectors.toList());
        itemRepo.saveAll(items);
        return getTransportRequest(trId);
    }

    @Transactional
    public RtsResponse createRts(String trId) {
        TransportRequest tr = findTr(trId);
        if (tr.getStatus() == TransportRequestStatus.RTS_CREATED)
            throw new RuntimeException("RTS already created for TR: " + trId);

        List<TransportRequestItem> trItems = itemRepo.findByTrId(trId);

        // Build RTS
        ReadyToShipOrder rts = ReadyToShipOrder.builder()
                .rtsNumber(rtsNumber())
                .trId(trId).cbrId(tr.getCbrId())
                .carrierId(tr.getCarrierId()).carrierName(tr.getCarrierName())
                .shipmentType(tr.getShipmentType())
                .shipper(ShipmentParty.builder()
                        .partyId(tr.getRequestedByPartyId()).partyName(tr.getRequestedByPartyName())
                        .partyType(tr.getRequestedByPartyType()).build())
                .originAddress(tr.getOriginAddress())
                .destinationAddress(tr.getDestinationAddress())
                .cargoReadyDateTime(tr.getCargoReadyDate() != null ? tr.getCargoReadyDate().atStartOfDay() : null)
                .loadType(tr.getLoadType())
                .totalWeightKg(tr.getTotalWeightKg()).totalVolumeM3(tr.getTotalVolumeM3())
                .totalPackages(tr.getTotalPackages())
                .freightPaymentCode("PREPAID")
                .status(RtsStatus.SUBMITTED)
                .asnSent(false)
                .build();
        ReadyToShipOrder savedRts = rtsRepo.save(rts);

        // Copy items to RTS
        List<ReadyToShipItem> rtsItems = trItems.stream().map(i -> ReadyToShipItem.builder()
                .rtsId(savedRts.getRtsId())
                .orderNumber(i.getOrderNumber()).orderLineId(i.getOrderLineId())
                .productId(i.getProductId()).productName(i.getProductName())
                .skuId(i.getSkuId()).variantName(i.getVariantName())
                .orderedQuantity(i.getOrderedQuantity()).packedQuantity(i.getPackedQuantity())
                .packageQuantity(i.getPackageQuantity())
                .weightKgPerUnit(i.getWeightKgPerUnit()).volumeM3PerUnit(i.getVolumeM3PerUnit())
                .isHazardous(Boolean.TRUE.equals(i.getIsHazardous())).build()
        ).collect(Collectors.toList());
        rtsItemRepo.saveAll(rtsItems);

        // Prepare (but do not send) the ASN for the destination party. It's only
        // actually sent once the carrier picks up the shipment — see
        // TransportShipmentService.postMilestone(), which flips this to SENT on
        // the PICKED milestone. Previously this fired at booking time, well
        // before the shipment had actually moved.
        AdvancedShipmentNotice asn = AdvancedShipmentNotice.builder()
                .asnNumber(asnNumber())
                .rtsId(savedRts.getRtsId()).rtsNumber(savedRts.getRtsNumber())
                .sentToPartyId(tr.getRequestedByPartyId()) // simplified: same party or override as needed
                .sentToPartyType(tr.getRequestedByPartyType())
                .sentToPartyName(tr.getRequestedByPartyName())
                .carrierId(tr.getCarrierId()).carrierName(tr.getCarrierName())
                .shipmentType(tr.getShipmentType())
                .originAddress(tr.getOriginAddress())
                .destinationAddress(tr.getDestinationAddress())
                .expectedArrivalDate(tr.getRequestedDeliveryDate())
                .totalWeightKg(tr.getTotalWeightKg()).totalVolumeM3(tr.getTotalVolumeM3())
                .totalPackages(tr.getTotalPackages())
                .status(AsnStatus.DRAFT).build();
        AdvancedShipmentNotice savedAsn = asnRepo.save(asn);

        // Create Transport Shipment
        TransportShipment ts = TransportShipment.builder()
                .tsNumber(tsNumber())
                .rtsId(savedRts.getRtsId()).rtsNumber(savedRts.getRtsNumber())
                .carrierId(tr.getCarrierId()).carrierName(tr.getCarrierName())
                .shipmentType(tr.getShipmentType())
                .shipper(ShipmentParty.builder()
                        .partyId(tr.getRequestedByPartyId()).partyName(tr.getRequestedByPartyName())
                        .partyType(tr.getRequestedByPartyType()).build())
                .originAddress(tr.getOriginAddress())
                .destinationAddress(tr.getDestinationAddress())
                .estimatedDeliveryDateTime(tr.getRequestedDeliveryDate() != null ? tr.getRequestedDeliveryDate().atStartOfDay() : null)
                .totalWeightKg(tr.getTotalWeightKg()).totalVolumeM3(tr.getTotalVolumeM3())
                .totalPackages(tr.getTotalPackages())
                .status(TransportShipmentStatus.CREATED).build();
        tsRepo.save(ts);

        // Update TR status
        tr.setStatus(TransportRequestStatus.RTS_CREATED);
        trRepo.save(tr);

        // Publish events
        producer.publishRtsCreated(savedRts.getRtsId(), Map.of(
                "rtsId", savedRts.getRtsId(), "rtsNumber", savedRts.getRtsNumber(),
                "carrierId", tr.getCarrierId() != null ? tr.getCarrierId() : "",
                "shipmentType", tr.getShipmentType().name(),
                "shippingOrderId", tr.getShippingOrderId() != null ? tr.getShippingOrderId() : "",
                "shippingOrderNumber", tr.getShippingOrderNumber() != null ? tr.getShippingOrderNumber() : "",
                "originCity", tr.getOriginAddress() != null && tr.getOriginAddress().getCity() != null ? tr.getOriginAddress().getCity() : "",
                "destinationCity", tr.getDestinationAddress() != null && tr.getDestinationAddress().getCity() != null ? tr.getDestinationAddress().getCity() : ""
        ));
        // ASN is sent (and transport.asn.sent published — this is also what
        // triggers workflowService's packing/grading orchestration) once the
        // carrier actually picks up the shipment, not here at booking time.
        // See TransportShipmentService.postMilestone().

        return toRtsResponse(savedRts, rtsItems);
    }

    private TransportRequest findTr(String trId) {
        return trRepo.findById(trId).orElseThrow(() -> new RuntimeException("TR not found: " + trId));
    }

    private TransportRequestItemDto toItemDto(TransportRequestItem i) {
        return TransportRequestItemDto.builder()
                .productId(i.getProductId()).productName(i.getProductName())
                .skuId(i.getSkuId()).variantName(i.getVariantName())
                .orderedQuantity(i.getOrderedQuantity()).packedQuantity(i.getPackedQuantity())
                .packageQuantity(i.getPackageQuantity())
                .weightKgPerUnit(i.getWeightKgPerUnit()).volumeM3PerUnit(i.getVolumeM3PerUnit())
                .isHazardous(i.getIsHazardous()).orderNumber(i.getOrderNumber()).orderLineId(i.getOrderLineId()).build();
    }

    private LocationAddressDto toAddrDto(LocationAddress a) {
        if (a == null) return null;
        return LocationAddressDto.builder().locationId(a.getLocationId()).street(a.getStreet())
                .city(a.getCity()).state(a.getState()).pincode(a.getPincode()).country(a.getCountry()).build();
    }

    private ShipmentPartyDto toPartyDto(ShipmentParty p) {
        if (p == null) return null;
        return ShipmentPartyDto.builder().partyId(p.getPartyId()).partyName(p.getPartyName())
                .partyType(p.getPartyType()).orgId(p.getOrgId()).contactPhone(p.getContactPhone()).build();
    }

    TransportRequestResponse toTrResponse(TransportRequest tr, List<TransportRequestItemDto> items) {
        return TransportRequestResponse.builder()
                .trId(tr.getTrId()).trNumber(tr.getTrNumber()).cbrId(tr.getCbrId())
                .cbrRespId(tr.getCbrRespId()).carrierId(tr.getCarrierId()).carrierName(tr.getCarrierName())
                .shippingOrderId(tr.getShippingOrderId()).shippingOrderNumber(tr.getShippingOrderNumber())
                .requestedByPartyId(tr.getRequestedByPartyId()).requestedByPartyType(tr.getRequestedByPartyType())
                .requestedByPartyName(tr.getRequestedByPartyName()).shipmentType(tr.getShipmentType())
                .originAddress(toAddrDto(tr.getOriginAddress())).destinationAddress(toAddrDto(tr.getDestinationAddress()))
                .cargoReadyDate(tr.getCargoReadyDate()).requestedPickupDate(tr.getRequestedPickupDate())
                .requestedDeliveryDate(tr.getRequestedDeliveryDate()).loadType(tr.getLoadType())
                .totalWeightKg(tr.getTotalWeightKg()).totalVolumeM3(tr.getTotalVolumeM3())
                .totalPackages(tr.getTotalPackages()).agreedRate(tr.getAgreedRate())
                .currency(tr.getCurrency()).status(tr.getStatus()).createdAt(tr.getCreatedAt())
                .items(items).build();
    }

    RtsResponse toRtsResponse(ReadyToShipOrder rts, List<ReadyToShipItem> items) {
        List<RtsItemDto> itemDtos = items.stream().map(i -> RtsItemDto.builder()
                .rtsItemId(i.getRtsItemId()).rtsId(i.getRtsId())
                .orderNumber(i.getOrderNumber()).orderLineId(i.getOrderLineId())
                .productId(i.getProductId()).productName(i.getProductName())
                .skuId(i.getSkuId()).variantName(i.getVariantName())
                .orderedQuantity(i.getOrderedQuantity()).packedQuantity(i.getPackedQuantity())
                .packageQuantity(i.getPackageQuantity())
                .weightKgPerUnit(i.getWeightKgPerUnit()).volumeM3PerUnit(i.getVolumeM3PerUnit())
                .isHazardous(i.getIsHazardous()).build()).collect(Collectors.toList());
        return RtsResponse.builder()
                .rtsId(rts.getRtsId()).rtsNumber(rts.getRtsNumber()).trId(rts.getTrId())
                .cbrId(rts.getCbrId()).carrierId(rts.getCarrierId()).carrierName(rts.getCarrierName())
                .shipmentType(rts.getShipmentType()).shipper(toPartyDto(rts.getShipper()))
                .consignee(toPartyDto(rts.getConsignee()))
                .originAddress(toAddrDto(rts.getOriginAddress())).destinationAddress(toAddrDto(rts.getDestinationAddress()))
                .cargoReadyDateTime(rts.getCargoReadyDateTime()).cargoCutoffDateTime(rts.getCargoCutoffDateTime())
                .loadType(rts.getLoadType()).totalWeightKg(rts.getTotalWeightKg())
                .totalVolumeM3(rts.getTotalVolumeM3()).totalPackages(rts.getTotalPackages())
                .incoterm(rts.getIncoterm()).freightPaymentCode(rts.getFreightPaymentCode())
                .status(rts.getStatus()).asnSent(rts.getAsnSent()).asnSentAt(rts.getAsnSentAt())
                .createdAt(rts.getCreatedAt()).items(itemDtos).build();
    }
}
