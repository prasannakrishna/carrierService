package com.bhagwat.scm.carrierService.service;

import com.bhagwat.scm.carrierService.dto.*;
import com.bhagwat.scm.carrierService.entity.*;
import com.bhagwat.scm.carrierService.enums.RtsStatus;
import com.bhagwat.scm.carrierService.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RtsService {

    private final ReadyToShipOrderRepository rtsRepository;
    private final ReadyToShipItemRepository itemRepository;

    @Transactional(readOnly = true)
    public RtsResponse getRts(String rtsId) {
        ReadyToShipOrder rts = findRts(rtsId);
        List<ReadyToShipItem> items = itemRepository.findByRtsId(rtsId);
        return toResponse(rts, items);
    }

    @Transactional(readOnly = true)
    public List<RtsResponse> listByCarrier(String carrierId) {
        return rtsRepository.findByCarrierId(carrierId).stream()
                .map(rts -> toResponse(rts, itemRepository.findByRtsId(rts.getRtsId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RtsResponse> listByStatus(RtsStatus status) {
        return rtsRepository.findByStatus(status).stream()
                .map(rts -> toResponse(rts, itemRepository.findByRtsId(rts.getRtsId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public RtsResponse approveRts(String rtsId) {
        ReadyToShipOrder rts = findRts(rtsId);
        if (rts.getStatus() != RtsStatus.DRAFT) {
            throw new RuntimeException("RTS can only be approved from DRAFT status, current: " + rts.getStatus());
        }
        rts.setStatus(RtsStatus.APPROVED);
        rtsRepository.save(rts);
        return toResponse(rts, itemRepository.findByRtsId(rtsId));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ReadyToShipOrder findRts(String rtsId) {
        return rtsRepository.findById(rtsId)
                .orElseThrow(() -> new RuntimeException("RTS not found: " + rtsId));
    }

    private RtsResponse toResponse(ReadyToShipOrder rts, List<ReadyToShipItem> items) {
        return RtsResponse.builder()
                .rtsId(rts.getRtsId())
                .rtsNumber(rts.getRtsNumber())
                .trId(rts.getTrId())
                .cbrId(rts.getCbrId())
                .carrierId(rts.getCarrierId())
                .carrierName(rts.getCarrierName())
                .shipmentType(rts.getShipmentType())
                .shipper(toPartyDto(rts.getShipper()))
                .consignee(toPartyDto(rts.getConsignee()))
                .originAddress(toAddressDto(rts.getOriginAddress()))
                .destinationAddress(toAddressDto(rts.getDestinationAddress()))
                .cargoReadyDateTime(rts.getCargoReadyDateTime())
                .cargoCutoffDateTime(rts.getCargoCutoffDateTime())
                .loadType(rts.getLoadType())
                .totalWeightKg(rts.getTotalWeightKg())
                .totalVolumeM3(rts.getTotalVolumeM3())
                .totalPackages(rts.getTotalPackages())
                .incoterm(rts.getIncoterm())
                .freightPaymentCode(rts.getFreightPaymentCode())
                .status(rts.getStatus())
                .asnSent(rts.getAsnSent())
                .asnSentAt(rts.getAsnSentAt())
                .createdAt(rts.getCreatedAt())
                .items(items.stream().map(this::toItemDto).collect(Collectors.toList()))
                .build();
    }

    private RtsItemDto toItemDto(ReadyToShipItem i) {
        return RtsItemDto.builder()
                .rtsItemId(i.getRtsItemId())
                .rtsId(i.getRtsId())
                .orderNumber(i.getOrderNumber())
                .orderLineId(i.getOrderLineId())
                .productId(i.getProductId())
                .productName(i.getProductName())
                .skuId(i.getSkuId())
                .variantName(i.getVariantName())
                .orderedQuantity(i.getOrderedQuantity())
                .packedQuantity(i.getPackedQuantity())
                .packageQuantity(i.getPackageQuantity())
                .weightKgPerUnit(i.getWeightKgPerUnit())
                .volumeM3PerUnit(i.getVolumeM3PerUnit())
                .isHazardous(i.getIsHazardous())
                .build();
    }

    private ShipmentPartyDto toPartyDto(ShipmentParty p) {
        if (p == null) return null;
        return ShipmentPartyDto.builder()
                .partyId(p.getPartyId()).partyName(p.getPartyName())
                .partyType(p.getPartyType()).orgId(p.getOrgId())
                .contactPhone(p.getContactPhone()).build();
    }

    private LocationAddressDto toAddressDto(LocationAddress a) {
        if (a == null) return null;
        return LocationAddressDto.builder()
                .locationId(a.getLocationId()).street(a.getStreet())
                .city(a.getCity()).state(a.getState())
                .pincode(a.getPincode()).country(a.getCountry()).build();
    }
}
