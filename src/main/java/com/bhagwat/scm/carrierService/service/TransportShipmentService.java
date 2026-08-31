package com.bhagwat.scm.carrierService.service;

import com.bhagwat.scm.carrierService.dto.*;
import com.bhagwat.scm.carrierService.entity.*;
import com.bhagwat.scm.carrierService.enums.*;
import com.bhagwat.scm.carrierService.kafka.CarrierKafkaProducer;
import com.bhagwat.scm.carrierService.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransportShipmentService {

    private final TransportShipmentRepository shipmentRepository;
    private final ShipmentMilestoneRepository milestoneRepository;
    private final ReadyToShipOrderRepository rtsRepository;
    private final ConsignmentRepository consignmentRepository;
    private final AdvancedShipmentNoticeRepository asnRepository;
    private final CarrierKafkaProducer kafkaProducer;

    // ── Queries ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public TransportShipmentResponse getShipment(String tsId) {
        TransportShipment ts = findShipment(tsId);
        List<ShipmentMilestone> milestones = milestoneRepository.findByTsIdOrderByMilestoneDateTimeAsc(tsId);
        return toResponse(ts, milestones);
    }

    @Transactional(readOnly = true)
    public List<TransportShipmentResponse> listByCarrier(String carrierId) {
        return shipmentRepository.findByCarrierId(carrierId).stream()
                .map(ts -> toResponse(ts, milestoneRepository.findByTsIdOrderByMilestoneDateTimeAsc(ts.getTsId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransportShipmentResponse> listByCarrierAndStatus(String carrierId, TransportShipmentStatus status) {
        return shipmentRepository.findByCarrierIdAndStatus(carrierId, status).stream()
                .map(ts -> toResponse(ts, milestoneRepository.findByTsIdOrderByMilestoneDateTimeAsc(ts.getTsId())))
                .collect(Collectors.toList());
    }

    // ── Milestone posting ────────────────────────────────────────────────────

    @Transactional
    public MilestoneResponse postMilestone(MilestoneRequest req) {
        if (req.getIdempotencyKey() != null) {
            var existing = milestoneRepository.findByIdempotencyKey(req.getIdempotencyKey());
            if (existing.isPresent()) {
                return toMilestoneResponse(existing.get());
            }
        }

        TransportShipment ts = findShipment(req.getTsId());

        ShipmentMilestone milestone = ShipmentMilestone.builder()
                .tsId(req.getTsId())
                .milestoneType(req.getMilestoneType())
                .milestoneDateTime(req.getMilestoneDateTime())
                .location(req.getLocation())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .notes(req.getNotes())
                .postedBy(req.getPostedBy())
                .idempotencyKey(req.getIdempotencyKey())
                .build();

        milestoneRepository.save(milestone);

        // Advance shipment status based on milestone
        TransportShipmentStatus newStatus = resolveStatus(req.getMilestoneType());
        if (newStatus != null) {
            ts.setStatus(newStatus);
            if (newStatus == TransportShipmentStatus.PICKED) {
                ts.setActualPickupDateTime(req.getMilestoneDateTime());
                sendAsnOnPickup(ts);
            } else if (newStatus == TransportShipmentStatus.DELIVERED) {
                ts.setActualDeliveryDateTime(req.getMilestoneDateTime());
                kafkaProducer.publishShipmentDelivered(ts.getTsId(), java.util.Map.of("tsId", ts.getTsId(), "rtsId", ts.getRtsId(), "carrierId", ts.getCarrierId()));
            } else if (newStatus == TransportShipmentStatus.DELIVERY_FAILED) {
                ts.setFailureReason(req.getNotes());
            }
            if (req.getLocation() != null) {
                ts.setCurrentLocation(req.getLocation());
            }
            shipmentRepository.save(ts);
            kafkaProducer.publishMilestone(ts.getTsId(), java.util.Map.of("tsId", ts.getTsId(), "milestone", req.getMilestoneType().name(), "location", req.getLocation() != null ? req.getLocation() : ""));
        }

        return toMilestoneResponse(milestone);
    }

    private TransportShipmentStatus resolveStatus(MilestoneType type) {
        return switch (type) {
            case PICKED -> TransportShipmentStatus.PICKED;
            case LOADED, DEPARTED_ORIGIN -> TransportShipmentStatus.SHIPPED;
            case IN_TRANSIT -> TransportShipmentStatus.IN_TRANSIT;
            case REACHED_HUB -> TransportShipmentStatus.REACHED_HUB;
            case OUT_FOR_DELIVERY -> TransportShipmentStatus.OUT_FOR_DELIVERY;
            case DELIVERED -> TransportShipmentStatus.DELIVERED;
            case DELIVERY_FAILED -> TransportShipmentStatus.DELIVERY_FAILED;
            case RETURNED_TO_ORIGIN -> TransportShipmentStatus.RETURNED;
            default -> null;
        };
    }

    // ── Plan assignment (called by Kafka consumer) ────────────────────────────

    /**
     * Creates a TransportShipment from a TransportOrder received via Kafka.
     * This is the bridge between planning (transportPlanner) and execution (carrierService).
     */
    @Transactional
    public String createFromTransportOrder(String toId, String toNumber, String planId,
                                            String carrierId, String vehicleId, String vehicleNumber,
                                            String driverName, java.util.Map<String, Object> event) {
        String tsNumber = "TS-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Extract locations from event
        LocationAddress origin = extractAddress(event, "orig_");
        LocationAddress dest = extractAddress(event, "dest_");

        TransportShipment ts = TransportShipment.builder()
                .tsNumber(tsNumber)
                .rtsId((String) event.get("rtsId"))
                .transportPlanId(planId)
                .transportPlanNumber((String) event.get("planNumber"))
                .carrierId(carrierId)
                .carrierName((String) event.get("carrierName"))
                .vehicleId(vehicleId)
                .vehicleNumber(vehicleNumber)
                .driverName(driverName)
                .driverPhone((String) event.get("driverPhone"))
                .shipmentType(event.get("shipmentType") != null ?
                        ShipmentType.valueOf((String) event.get("shipmentType")) : null)
                .originAddress(origin)
                .destinationAddress(dest)
                .totalWeightKg(toBigDecimal(event.get("totalWeightKg")))
                .totalVolumeM3(toBigDecimal(event.get("totalVolumeM3")))
                .totalPackages(event.get("totalPackages") != null ?
                        ((Number) event.get("totalPackages")).intValue() : null)
                .status(TransportShipmentStatus.CREATED)
                .build();

        TransportShipment saved = shipmentRepository.save(ts);

        // Every shipment has at least one destination-level consignment. The live
        // transport-planning path only ever produces DIRECT plans today (single
        // origin, single destination), so one consignment per shipment matches
        // actual current behavior; SOURCE/DEST_CONSOLIDATION would add more here
        // once the planner's plan-type selection is wired into the live path.
        Consignment consignment = Consignment.builder()
                .transportShipmentId(saved.getTsId())
                .orderId((String) event.get("orderId"))
                .destinationType(dest != null && dest.getLocationId() != null ? "PARTY" : "UNKNOWN")
                .destinationId(saved.getConsignee() != null && saved.getConsignee().getPartyId() != null
                        ? saved.getConsignee().getPartyId() : "unknown")
                .destinationName(saved.getConsignee() != null ? saved.getConsignee().getPartyName() : null)
                .labelCode("CNG-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .totalPackages(saved.getTotalPackages())
                .totalWeightKg(saved.getTotalWeightKg())
                .build();
        consignmentRepository.save(consignment);

        return saved.getTsId();
    }

    private LocationAddress extractAddress(java.util.Map<String, Object> event, String prefix) {
        String city = (String) event.get(prefix + "city");
        if (city == null) return null;
        LocationAddress addr = new LocationAddress();
        addr.setLocationId((String) event.get(prefix + "location_id"));
        addr.setStreet((String) event.get(prefix + "street"));
        addr.setCity(city);
        addr.setState((String) event.get(prefix + "state"));
        addr.setPincode((String) event.get(prefix + "pincode"));
        addr.setCountry((String) event.get(prefix + "country"));
        return addr;
    }

    private java.math.BigDecimal toBigDecimal(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return java.math.BigDecimal.valueOf(((Number) val).doubleValue());
        return new java.math.BigDecimal(val.toString());
    }

    @Transactional
    public void assignTransportPlanByRtsId(String rtsId, String transportPlanId, String transportPlanNumber) {
        List<TransportShipment> shipments = shipmentRepository.findByTransportPlanId(rtsId);
        // fallback: find by rtsId match in the table (rtsId field)
        List<TransportShipment> all = shipmentRepository.findAll().stream()
                .filter(ts -> rtsId.equals(ts.getRtsId()))
                .collect(Collectors.toList());
        if (!all.isEmpty()) {
            all.forEach(ts -> {
                ts.setTransportPlanId(transportPlanId);
                ts.setTransportPlanNumber(transportPlanNumber);
                shipmentRepository.save(ts);
            });
        }
    }

    // ── Vehicle assignment ────────────────────────────────────────────────────

    @Transactional
    public TransportShipmentResponse assignVehicle(String tsId, String vehicleId, String vehicleNumber,
                                                    String driverName, String driverPhone) {
        TransportShipment ts = findShipment(tsId);
        ts.setVehicleId(vehicleId);
        ts.setVehicleNumber(vehicleNumber);
        ts.setDriverName(driverName);
        ts.setDriverPhone(driverPhone);
        shipmentRepository.save(ts);
        return toResponse(ts, milestoneRepository.findByTsIdOrderByMilestoneDateTimeAsc(tsId));
    }

    // ── Location update ───────────────────────────────────────────────────────

    @Transactional
    public TransportShipmentResponse updateLocation(String tsId, String location) {
        TransportShipment ts = findShipment(tsId);
        ts.setCurrentLocation(location);
        shipmentRepository.save(ts);
        return toResponse(ts, milestoneRepository.findByTsIdOrderByMilestoneDateTimeAsc(tsId));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * ASN is created (as DRAFT) at RTS booking time but only actually sent to
     * the consignee once the carrier picks up the shipment — see
     * TransportRequestService.createRts(). This also publishes
     * transport.asn.sent, which is what triggers workflowService's
     * packing/grading orchestration, so that now correctly fires on real
     * pickup instead of at booking.
     */
    private void sendAsnOnPickup(TransportShipment ts) {
        if (ts.getRtsId() == null) return;
        List<AdvancedShipmentNotice> draftAsns = asnRepository.findByRtsId(ts.getRtsId()).stream()
                .filter(a -> a.getStatus() == AsnStatus.DRAFT)
                .toList();
        for (AdvancedShipmentNotice asn : draftAsns) {
            asn.setStatus(AsnStatus.SENT);
            asn.setSentAt(java.time.LocalDateTime.now());
            asnRepository.save(asn);
            kafkaProducer.publishAsnSent(asn.getAsnId(), java.util.Map.of("asnId", asn.getAsnId(), "rtsId", ts.getRtsId()));
        }
        rtsRepository.findById(ts.getRtsId()).ifPresent(rts -> {
            rts.setAsnSent(true);
            rts.setAsnSentAt(java.time.LocalDateTime.now());
            rtsRepository.save(rts);
        });
    }

    private TransportShipment findShipment(String tsId) {
        return shipmentRepository.findById(tsId)
                .orElseThrow(() -> new RuntimeException("Transport shipment not found: " + tsId));
    }

    private TransportShipmentResponse toResponse(TransportShipment ts, List<ShipmentMilestone> milestones) {
        return TransportShipmentResponse.builder()
                .tsId(ts.getTsId())
                .tsNumber(ts.getTsNumber())
                .rtsId(ts.getRtsId())
                .rtsNumber(ts.getRtsNumber())
                .transportPlanId(ts.getTransportPlanId())
                .transportPlanNumber(ts.getTransportPlanNumber())
                .carrierId(ts.getCarrierId())
                .carrierName(ts.getCarrierName())
                .vehicleId(ts.getVehicleId())
                .vehicleNumber(ts.getVehicleNumber())
                .driverName(ts.getDriverName())
                .driverPhone(ts.getDriverPhone())
                .shipmentType(ts.getShipmentType())
                .shipper(toPartyDto(ts.getShipper()))
                .consignee(toPartyDto(ts.getConsignee()))
                .originAddress(toAddressDto(ts.getOriginAddress()))
                .destinationAddress(toAddressDto(ts.getDestinationAddress()))
                .actualPickupDateTime(ts.getActualPickupDateTime())
                .estimatedDeliveryDateTime(ts.getEstimatedDeliveryDateTime())
                .actualDeliveryDateTime(ts.getActualDeliveryDateTime())
                .totalWeightKg(ts.getTotalWeightKg())
                .totalVolumeM3(ts.getTotalVolumeM3())
                .totalPackages(ts.getTotalPackages())
                .currentLocation(ts.getCurrentLocation())
                .status(ts.getStatus())
                .failureReason(ts.getFailureReason())
                .createdAt(ts.getCreatedAt())
                .milestones(milestones.stream().map(this::toMilestoneResponse).collect(Collectors.toList()))
                .build();
    }

    private MilestoneResponse toMilestoneResponse(ShipmentMilestone m) {
        return MilestoneResponse.builder()
                .milestoneId(m.getMilestoneId())
                .tsId(m.getTsId())
                .milestoneType(m.getMilestoneType())
                .milestoneDateTime(m.getMilestoneDateTime())
                .location(m.getLocation())
                .latitude(m.getLatitude())
                .longitude(m.getLongitude())
                .notes(m.getNotes())
                .postedBy(m.getPostedBy())
                .createdAt(m.getCreatedAt())
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
