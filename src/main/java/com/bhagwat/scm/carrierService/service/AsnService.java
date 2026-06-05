package com.bhagwat.scm.carrierService.service;

import com.bhagwat.scm.carrierService.dto.*;
import com.bhagwat.scm.carrierService.entity.*;
import com.bhagwat.scm.carrierService.enums.AsnStatus;
import com.bhagwat.scm.carrierService.repository.AdvancedShipmentNoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AsnService {

    private final AdvancedShipmentNoticeRepository asnRepository;

    @Transactional(readOnly = true)
    public AsnResponse getAsn(String asnId) {
        return toResponse(findAsn(asnId));
    }

    @Transactional(readOnly = true)
    public List<AsnResponse> listByParty(String partyId) {
        return asnRepository.findBySentToPartyId(partyId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AsnResponse> listByPartyAndStatus(String partyId, AsnStatus status) {
        return asnRepository.findBySentToPartyIdAndStatus(partyId, status).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AsnResponse> listByRts(String rtsId) {
        return asnRepository.findByRtsId(rtsId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public AsnResponse acknowledgeAsn(String asnId) {
        AdvancedShipmentNotice asn = findAsn(asnId);
        if (asn.getStatus() != AsnStatus.SENT) {
            throw new RuntimeException("ASN already acknowledged or in invalid state: " + asn.getStatus());
        }
        asn.setStatus(AsnStatus.ACKNOWLEDGED);
        asn.setAcknowledgedAt(LocalDateTime.now());
        asnRepository.save(asn);
        return toResponse(asn);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AdvancedShipmentNotice findAsn(String asnId) {
        return asnRepository.findById(asnId)
                .orElseThrow(() -> new RuntimeException("ASN not found: " + asnId));
    }

    private AsnResponse toResponse(AdvancedShipmentNotice asn) {
        return AsnResponse.builder()
                .asnId(asn.getAsnId())
                .asnNumber(asn.getAsnNumber())
                .rtsId(asn.getRtsId())
                .rtsNumber(asn.getRtsNumber())
                .sentToPartyId(asn.getSentToPartyId())
                .sentToPartyType(asn.getSentToPartyType())
                .sentToPartyName(asn.getSentToPartyName())
                .carrierId(asn.getCarrierId())
                .carrierName(asn.getCarrierName())
                .shipmentType(asn.getShipmentType())
                .originAddress(toAddressDto(asn.getOriginAddress()))
                .destinationAddress(toAddressDto(asn.getDestinationAddress()))
                .expectedArrivalDate(asn.getExpectedArrivalDate())
                .deliveryWindow(asn.getDeliveryWindow())
                .totalWeightKg(asn.getTotalWeightKg())
                .totalVolumeM3(asn.getTotalVolumeM3())
                .totalPackages(asn.getTotalPackages())
                .status(asn.getStatus())
                .sentAt(asn.getSentAt())
                .acknowledgedAt(asn.getAcknowledgedAt())
                .createdAt(asn.getCreatedAt())
                .build();
    }

    private LocationAddressDto toAddressDto(LocationAddress a) {
        if (a == null) return null;
        return LocationAddressDto.builder()
                .locationId(a.getLocationId()).street(a.getStreet())
                .city(a.getCity()).state(a.getState())
                .pincode(a.getPincode()).country(a.getCountry()).build();
    }
}
