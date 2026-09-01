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
public class CarrierBookingService {

    private final CarrierBookingRequestRepository cbrRepo;
    private final CarrierBookingBroadcastRepository broadcastRepo;
    private final CarrierBookingResponseRepository respRepo;
    private final TransportRequestRepository trRepo;
    private final CarrierRepository carrierRepo;
    private final CarrierKafkaProducer producer;

    private String cbrNumber()  { return String.format("CBR-%d-%05d", Year.now().getValue(), cbrRepo.count() + 1); }
    private String cbrpNumber() { return String.format("CBRP-%d-%05d", Year.now().getValue(), respRepo.count() + 1); }
    private String trNumber()   { return String.format("TR-%d-%05d", Year.now().getValue(), trRepo.count() + 1); }

    @Transactional
    public CbrResponse createCbr(CbrRequest req) {
        CarrierBookingRequest cbr = CarrierBookingRequest.builder()
                .cbrNumber(cbrNumber())
                .requestedByPartyId(req.getRequestedByPartyId())
                .requestedByPartyType(req.getRequestedByPartyType())
                .requestedByPartyName(req.getRequestedByPartyName())
                .shipmentType(req.getShipmentType())
                .originAddress(toAddr(req.getOriginAddress()))
                .destinationAddress(toAddr(req.getDestinationAddress()))
                .cargoReadyDate(req.getCargoReadyDate())
                .requestedPickupDate(req.getRequestedPickupDate())
                .requestedDeliveryDate(req.getRequestedDeliveryDate())
                .loadType(req.getLoadType())
                .totalWeightKg(req.getTotalWeightKg())
                .totalVolumeM3(req.getTotalVolumeM3())
                .totalPackages(req.getTotalPackages())
                .specialInstructions(req.getSpecialInstructions())
                .contractId(req.getContractId())
                .status(CbrStatus.DRAFT)
                .build();
        return toCbrResponse(cbrRepo.save(cbr));
    }

    @Transactional
    public CbrResponse broadcastCbr(String cbrId, List<String> carrierIds) {
        CarrierBookingRequest cbr = findCbr(cbrId);
        for (String cid : carrierIds) {
            carrierRepo.findById(cid).ifPresent(c -> {
                CarrierBookingBroadcast b = CarrierBookingBroadcast.builder()
                        .cbrId(cbrId).carrierId(cid).carrierName(c.getCarrierName())
                        .sentAt(LocalDateTime.now()).status(BroadcastStatus.SENT).build();
                broadcastRepo.save(b);
            });
        }
        cbr.setStatus(CbrStatus.BROADCAST);
        cbr.setBroadcastedAt(LocalDateTime.now());
        CarrierBookingRequest saved = cbrRepo.save(cbr);
        producer.publishCbrBroadcast(cbrId, Map.of("cbrId", cbrId, "carrierIds", carrierIds));
        return toCbrResponse(saved);
    }

    @Transactional
    public CbrResponseDto submitCarrierResponse(CbrResponseRequest req) {
        CarrierBookingResponse resp = CarrierBookingResponse.builder()
                .cbrRespNumber(cbrpNumber())
                .cbrId(req.getCbrId())
                .carrierId(req.getCarrierId())
                .carrierName(req.getCarrierName())
                .vehicleId(req.getVehicleId())
                .offeredPickupDate(req.getOfferedPickupDate())
                .offeredDeliveryDate(req.getOfferedDeliveryDate())
                .rateOffered(req.getRateOffered())
                .currency(req.getCurrency() != null ? req.getCurrency() : "INR")
                .rateType(req.getRateType())
                .notes(req.getNotes())
                .status(CbrRespStatus.ACCEPTED_BY_CARRIER)
                .respondedAt(LocalDateTime.now())
                .build();
        CarrierBookingResponse saved = respRepo.save(resp);
        // Mark CBR as responded
        cbrRepo.findById(req.getCbrId()).ifPresent(cbr -> {
            cbr.setStatus(CbrStatus.RESPONDED);
            cbrRepo.save(cbr);
        });
        // Update broadcast status
        broadcastRepo.findByCbrId(req.getCbrId()).stream()
                .filter(b -> b.getCarrierId().equals(req.getCarrierId()))
                .forEach(b -> { b.setStatus(BroadcastStatus.RESPONDED); broadcastRepo.save(b); });
        return toCbrRespDto(saved);
    }

    @Transactional
    public void declineResponse(String broadcastId) {
        CarrierBookingBroadcast b = broadcastRepo.findById(broadcastId)
                .orElseThrow(() -> new RuntimeException("Broadcast not found: " + broadcastId));
        b.setStatus(BroadcastStatus.DECLINED);
        broadcastRepo.save(b);
    }

    @Transactional(readOnly = true)
    public List<CbrResponseDto> listResponsesForCbr(String cbrId) {
        return respRepo.findByCbrId(cbrId).stream().map(this::toCbrRespDto).collect(Collectors.toList());
    }

    @Transactional
    public TransportRequestResponse acceptResponse(String cbrId, AcceptCbrResponseRequest req) {
        CarrierBookingResponse resp = respRepo.findById(req.getCbrRespId())
                .orElseThrow(() -> new RuntimeException("Response not found: " + req.getCbrRespId()));
        CarrierBookingRequest cbr = findCbr(cbrId);

        // This flow and the automatic one (ShippingOrderReadyConsumer) are two
        // independent ways a TransportRequest gets created for the same
        // shipping order — guard against booking the same one twice rather
        // than silently creating a duplicate TR (and, downstream, a duplicate
        // shipment) for it.
        if (req.getShippingOrderId() != null && !req.getShippingOrderId().isBlank()) {
            List<TransportRequest> existing = trRepo.findByShippingOrderId(req.getShippingOrderId());
            if (!existing.isEmpty()) {
                throw new RuntimeException("Shipping order " + req.getShippingOrderId()
                        + " already has a transport request (trId=" + existing.get(0).getTrId()
                        + ") — it may already be booked via the automatic carrier-selection flow.");
            }
        }

        resp.setStatus(CbrRespStatus.ACCEPTED_BY_SHIPPER);
        respRepo.save(resp);
        cbr.setStatus(CbrStatus.ACCEPTED);
        cbrRepo.save(cbr);

        TransportRequest tr = TransportRequest.builder()
                .trNumber(trNumber())
                .cbrId(cbrId)
                .cbrRespId(resp.getCbrRespId())
                .carrierId(resp.getCarrierId())
                .carrierName(resp.getCarrierName())
                .shippingOrderId(req.getShippingOrderId())
                .shippingOrderNumber(req.getShippingOrderNumber())
                .requestedByPartyId(cbr.getRequestedByPartyId())
                .requestedByPartyType(cbr.getRequestedByPartyType())
                .requestedByPartyName(cbr.getRequestedByPartyName())
                .shipmentType(cbr.getShipmentType())
                .originAddress(cbr.getOriginAddress())
                .destinationAddress(cbr.getDestinationAddress())
                .cargoReadyDate(cbr.getCargoReadyDate())
                .requestedPickupDate(resp.getOfferedPickupDate())
                .requestedDeliveryDate(resp.getOfferedDeliveryDate())
                .loadType(cbr.getLoadType())
                .totalWeightKg(cbr.getTotalWeightKg())
                .totalVolumeM3(cbr.getTotalVolumeM3())
                .totalPackages(cbr.getTotalPackages())
                .contractId(cbr.getContractId())
                .agreedRate(resp.getRateOffered())
                .currency(resp.getCurrency())
                .status(TransportRequestStatus.PENDING)
                .build();
        return toTrResponse(trRepo.save(tr));
    }

    @Transactional(readOnly = true)
    public CbrResponse getCbr(String cbrId) { return toCbrResponse(findCbr(cbrId)); }

    @Transactional(readOnly = true)
    public List<CbrResponse> listCbrs(String partyId, CbrStatus status) {
        List<CarrierBookingRequest> list;
        if (partyId != null && status != null) list = cbrRepo.findByRequestedByPartyIdAndStatus(partyId, status);
        else if (partyId != null) list = cbrRepo.findByRequestedByPartyId(partyId);
        else if (status != null) list = cbrRepo.findByStatus(status);
        else list = cbrRepo.findAll();
        return list.stream().map(this::toCbrResponse).collect(Collectors.toList());
    }

    private CarrierBookingRequest findCbr(String cbrId) {
        return cbrRepo.findById(cbrId).orElseThrow(() -> new RuntimeException("CBR not found: " + cbrId));
    }

    private LocationAddress toAddr(LocationAddressDto dto) {
        if (dto == null) return null;
        return LocationAddress.builder().locationId(dto.getLocationId()).street(dto.getStreet())
                .city(dto.getCity()).state(dto.getState()).pincode(dto.getPincode()).country(dto.getCountry()).build();
    }

    private LocationAddressDto toAddrDto(LocationAddress a) {
        if (a == null) return null;
        return LocationAddressDto.builder().locationId(a.getLocationId()).street(a.getStreet())
                .city(a.getCity()).state(a.getState()).pincode(a.getPincode()).country(a.getCountry()).build();
    }

    CbrResponse toCbrResponse(CarrierBookingRequest c) {
        List<BroadcastDto> broadcasts = broadcastRepo.findByCbrId(c.getCbrId()).stream()
                .map(b -> BroadcastDto.builder().broadcastId(b.getBroadcastId())
                        .carrierId(b.getCarrierId()).carrierName(b.getCarrierName())
                        .sentAt(b.getSentAt()).status(b.getStatus()).build())
                .collect(Collectors.toList());
        return CbrResponse.builder()
                .cbrId(c.getCbrId()).cbrNumber(c.getCbrNumber())
                .requestedByPartyId(c.getRequestedByPartyId())
                .requestedByPartyType(c.getRequestedByPartyType())
                .requestedByPartyName(c.getRequestedByPartyName())
                .shipmentType(c.getShipmentType())
                .originAddress(toAddrDto(c.getOriginAddress()))
                .destinationAddress(toAddrDto(c.getDestinationAddress()))
                .cargoReadyDate(c.getCargoReadyDate())
                .requestedPickupDate(c.getRequestedPickupDate())
                .requestedDeliveryDate(c.getRequestedDeliveryDate())
                .loadType(c.getLoadType()).totalWeightKg(c.getTotalWeightKg())
                .totalVolumeM3(c.getTotalVolumeM3()).totalPackages(c.getTotalPackages())
                .specialInstructions(c.getSpecialInstructions()).contractId(c.getContractId())
                .status(c.getStatus()).broadcastedAt(c.getBroadcastedAt())
                .createdAt(c.getCreatedAt()).broadcasts(broadcasts).build();
    }

    CbrResponseDto toCbrRespDto(CarrierBookingResponse r) {
        return CbrResponseDto.builder()
                .cbrRespId(r.getCbrRespId()).cbrRespNumber(r.getCbrRespNumber())
                .cbrId(r.getCbrId()).carrierId(r.getCarrierId()).carrierName(r.getCarrierName())
                .vehicleId(r.getVehicleId()).offeredPickupDate(r.getOfferedPickupDate())
                .offeredDeliveryDate(r.getOfferedDeliveryDate()).rateOffered(r.getRateOffered())
                .currency(r.getCurrency()).rateType(r.getRateType()).notes(r.getNotes())
                .status(r.getStatus()).respondedAt(r.getRespondedAt()).createdAt(r.getCreatedAt()).build();
    }

    TransportRequestResponse toTrResponse(TransportRequest tr) {
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
                .currency(tr.getCurrency()).status(tr.getStatus()).createdAt(tr.getCreatedAt()).build();
    }
}
