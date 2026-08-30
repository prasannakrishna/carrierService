package com.bhagwat.scm.carrierService.service;

import com.bhagwat.scm.carrierService.dto.*;
import com.bhagwat.scm.carrierService.entity.*;
import com.bhagwat.scm.carrierService.enums.ConsignmentStatus;
import com.bhagwat.scm.carrierService.enums.MilestoneType;
import com.bhagwat.scm.carrierService.enums.TransportShipmentStatus;
import com.bhagwat.scm.carrierService.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Backs carrier-driver-app's Retrofit contract (CarrierDriverApi.kt) against the
 * real transport-shipment/consignment data model, so the app can actually reach
 * its own backend instead of calling endpoints that don't exist.
 */
@Service
@RequiredArgsConstructor
public class DriverAppService {

    private final DriverRepository driverRepository;
    private final CarrierVehicleRepository vehicleRepository;
    private final CarrierRepository carrierRepository;
    private final TransportShipmentRepository shipmentRepository;
    private final ShipmentMilestoneRepository milestoneRepository;
    private final ConsignmentRepository consignmentRepository;
    private final DeliveryReportRepository deliveryReportRepository;
    private final DriverFieldExceptionRepository exceptionRepository;
    private final TransportShipmentService transportShipmentService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    // ── Auth ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public DriverSessionResponse login(DriverLoginRequest req) {
        CarrierVehicle vehicle = vehicleRepository.findByVehicleNumber(req.getVehicleNumber())
                .orElseThrow(() -> new IllegalArgumentException("Unknown vehicle number: " + req.getVehicleNumber()));
        if (vehicle.getDriverId() == null) {
            throw new IllegalStateException("No driver is currently assigned to vehicle " + req.getVehicleNumber());
        }
        Driver driver = driverRepository.findById(vehicle.getDriverId())
                .orElseThrow(() -> new IllegalStateException("Assigned driver record not found: " + vehicle.getDriverId()));
        if (driver.getPinHash() == null || !passwordEncoder.matches(req.getDriverPin(), driver.getPinHash())) {
            throw new IllegalArgumentException("Invalid driver PIN");
        }

        String carrierName = carrierRepository.findById(vehicle.getCarrierId())
                .map(Carrier::getCarrierName).orElse(vehicle.getCarrierId());

        return DriverSessionResponse.builder()
                .driverId(driver.getDriverId())
                .driverName(driver.getName())
                .carrierId(vehicle.getCarrierId())
                .carrierName(carrierName)
                .vehicleId(vehicle.getVehicleId())
                .vehicleNumber(vehicle.getVehicleNumber())
                .vehicleType(vehicle.getVehicleType() != null ? vehicle.getVehicleType().name() : null)
                .token(java.util.UUID.randomUUID().toString())
                .build();
    }

    // ── Shipments ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DriverShipmentDto> getActiveShipments(String vehicleId) {
        return shipmentRepository.findByVehicleId(vehicleId).stream()
                .filter(ts -> ts.getStatus() != TransportShipmentStatus.DELIVERED
                        && ts.getStatus() != TransportShipmentStatus.DELIVERY_FAILED
                        && ts.getStatus() != TransportShipmentStatus.RETURNED)
                .map(this::toDriverShipmentDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DriverShipmentDto getShipmentDetail(String tsId) {
        return toDriverShipmentDto(findShipment(tsId));
    }

    // ── Consignments ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ConsignmentDto> getConsignments(String tsId) {
        return consignmentRepository.findByTransportShipmentId(tsId).stream()
                .map(this::toConsignmentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ConsignmentLoadResult scanLoad(String tsId, String barcode) {
        Consignment c = consignmentRepository.findByLabelCode(barcode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown consignment barcode: " + barcode));
        if (!c.getTransportShipmentId().equals(tsId)) {
            return ConsignmentLoadResult.builder()
                    .consignmentId(c.getConsignmentId()).status("REJECTED")
                    .message("This consignment belongs to a different shipment").build();
        }
        c.setStatus(ConsignmentStatus.LOADED);
        consignmentRepository.save(c);
        return ConsignmentLoadResult.builder()
                .consignmentId(c.getConsignmentId()).status("LOADED")
                .message("Scanned and marked loaded").build();
    }

    @Transactional
    public DriverShipmentDto markLoaded(String tsId) {
        List<Consignment> consignments = consignmentRepository.findByTransportShipmentId(tsId);
        boolean allLoaded = !consignments.isEmpty()
                && consignments.stream().allMatch(c -> c.getStatus() != ConsignmentStatus.MANIFESTED);
        if (!allLoaded) {
            throw new IllegalStateException("Not every consignment on this shipment has been scanned yet");
        }
        transportShipmentService.postMilestone(MilestoneRequest.builder()
                .tsId(tsId)
                .milestoneType(MilestoneType.LOADED)
                .milestoneDateTime(LocalDateTime.now())
                .build());
        return getShipmentDetail(tsId);
    }

    @Transactional
    public ConsignmentDto confirmUnload(String consignmentId) {
        Consignment c = consignmentRepository.findById(consignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown consignment: " + consignmentId));
        c.setStatus(ConsignmentStatus.UNLOADED);
        consignmentRepository.save(c);
        return toConsignmentDto(c);
    }

    // ── Milestones & arrival ─────────────────────────────────────────────────

    @Transactional
    public DriverMilestoneResponse postMilestone(String tsId, DriverMilestoneRequest req, String driverId) {
        MilestoneType type;
        try {
            type = MilestoneType.valueOf(req.getMilestoneType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown milestone type: " + req.getMilestoneType());
        }
        MilestoneResponse resp = transportShipmentService.postMilestone(MilestoneRequest.builder()
                .tsId(tsId)
                .milestoneType(type)
                .milestoneDateTime(LocalDateTime.now())
                .latitude(req.getLatitude() != null ? BigDecimal.valueOf(req.getLatitude()) : null)
                .longitude(req.getLongitude() != null ? BigDecimal.valueOf(req.getLongitude()) : null)
                .notes(req.getNotes())
                .postedBy(driverId)
                .idempotencyKey(req.getIdempotencyKey())
                .build());
        return DriverMilestoneResponse.builder()
                .milestoneId(resp.getMilestoneId())
                .tsId(resp.getTsId())
                .milestoneType(resp.getMilestoneType() != null ? resp.getMilestoneType().name() : null)
                .notes(resp.getNotes())
                .postedAt(resp.getCreatedAt() != null ? resp.getCreatedAt().toString() : null)
                .build();
    }

    @Transactional
    public DriverShipmentDto arriveAtStop(String tsId, String stopId) {
        // No separate multi-stop RouteStop model exists yet — today's live planning
        // path only ever produces single-destination DIRECT shipments (see
        // TransportShipmentService.createFromTransportOrder). Until that's wired up,
        // "arrival" records against the shipment's single destination directly.
        transportShipmentService.updateLocation(tsId, stopId);
        return getShipmentDetail(tsId);
    }

    // ── Delivery report (mutual attestation) ────────────────────────────────

    @Transactional
    public DeliveryReportDto submitDeliveryReport(DeliveryReportRequestDto req, String driverId) {
        var existing = deliveryReportRepository.findByIdempotencyKey(req.getIdempotencyKey());
        if (existing.isPresent()) {
            return toDeliveryReportDto(existing.get());
        }
        Consignment c = consignmentRepository.findById(req.getConsignmentId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown consignment: " + req.getConsignmentId()));

        DeliveryReport report = DeliveryReport.builder()
                .consignmentId(req.getConsignmentId())
                .countReported(req.getCountReported())
                .conditionNotes(req.getConditionNotes())
                .photos(req.getPhotoUris() != null ? String.join("|", req.getPhotoUris()) : null)
                .submittedBy(driverId)
                .idempotencyKey(req.getIdempotencyKey())
                .build();
        DeliveryReport saved = deliveryReportRepository.save(report);

        c.setStatus(ConsignmentStatus.DELIVERED);
        consignmentRepository.save(c);

        return toDeliveryReportDto(saved);
    }

    @Transactional(readOnly = true)
    public DeliveryReportDto getDeliveryReportByConsignment(String consignmentId) {
        DeliveryReport report = deliveryReportRepository.findByConsignmentId(consignmentId)
                .orElseThrow(() -> new IllegalArgumentException("No delivery report found for consignment: " + consignmentId));
        return toDeliveryReportDto(report);
    }

    // ── Exceptions ───────────────────────────────────────────────────────────

    @Transactional
    public void raiseException(String tsId, TransportExceptionRequest req, String raisedBy) {
        exceptionRepository.save(DriverFieldException.builder()
                .tsId(tsId)
                .consignmentId(req.getConsignmentId())
                .exceptionType(req.getExceptionType())
                .notes(req.getNotes())
                .photos(req.getPhotoUris() != null ? String.join("|", req.getPhotoUris()) : null)
                .latitude(req.getLatitude() != null ? BigDecimal.valueOf(req.getLatitude()) : null)
                .longitude(req.getLongitude() != null ? BigDecimal.valueOf(req.getLongitude()) : null)
                .raisedBy(raisedBy)
                .build());
    }

    // ── Verification rules ───────────────────────────────────────────────────

    public Map<String, Object> getVerificationRules(String consignmentId) {
        // No seller/product-configured verification-rules subsystem exists in the
        // platform yet. Real, honest default until one is built: every delivery
        // requires a photo and either a signature or an OTP.
        return Map.of(
                "requiresPhoto", true,
                "requiresSignatureOrOtp", true,
                "requiresCountConfirmation", true
        );
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private TransportShipment findShipment(String tsId) {
        return shipmentRepository.findById(tsId)
                .orElseThrow(() -> new IllegalArgumentException("Transport shipment not found: " + tsId));
    }

    private DriverShipmentDto toDriverShipmentDto(TransportShipment ts) {
        List<Consignment> consignments = consignmentRepository.findByTransportShipmentId(ts.getTsId());
        long loaded = consignments.stream()
                .filter(c -> c.getStatus() != ConsignmentStatus.MANIFESTED)
                .count();
        String lastMilestoneAt = milestoneRepository.findByTsIdOrderByMilestoneDateTimeAsc(ts.getTsId()).stream()
                .max(Comparator.comparing(ShipmentMilestone::getMilestoneDateTime))
                .map(m -> m.getMilestoneDateTime().toString())
                .orElse(null);

        return DriverShipmentDto.builder()
                .tsId(ts.getTsId())
                .tsNumber(ts.getTsNumber())
                .carrierId(ts.getCarrierId())
                .vehicleId(ts.getVehicleId())
                .vehicleNumber(ts.getVehicleNumber())
                .driverId(null) // TransportShipment stores driverName/phone only, not a driverId FK today
                .status(ts.getStatus() != null ? ts.getStatus().name() : null)
                .totalConsignments(consignments.size())
                .loadedConsignments((int) loaded)
                .plannedPickup(null)
                .plannedDelivery(ts.getEstimatedDeliveryDateTime() != null ? ts.getEstimatedDeliveryDateTime().toString() : null)
                .lastMilestoneAt(lastMilestoneAt)
                .routePlan(null) // single-destination DIRECT shipments today — no multi-stop route to report
                .build();
    }

    private ConsignmentDto toConsignmentDto(Consignment c) {
        return ConsignmentDto.builder()
                .consignmentId(c.getConsignmentId())
                .transportShipmentId(c.getTransportShipmentId())
                .orderId(c.getOrderId())
                .destinationType(c.getDestinationType())
                .destinationId(c.getDestinationId())
                .destinationName(c.getDestinationName())
                .labelCode(c.getLabelCode())
                .status(c.getStatus() != null ? c.getStatus().name() : null)
                .items(c.getItems() == null ? List.of() : c.getItems().stream()
                        .map(i -> ConsignmentItemDto.builder()
                                .skuId(i.getSkuId())
                                .productName(i.getProductName())
                                .quantity(i.getQuantity())
                                .weightKg(i.getWeightKg() != null ? i.getWeightKg().doubleValue() : null)
                                .build())
                        .collect(Collectors.toList()))
                .totalPackages(c.getTotalPackages())
                .totalWeightKg(c.getTotalWeightKg() != null ? c.getTotalWeightKg().doubleValue() : null)
                .build();
    }

    private DeliveryReportDto toDeliveryReportDto(DeliveryReport r) {
        return DeliveryReportDto.builder()
                .reportId(r.getReportId())
                .consignmentId(r.getConsignmentId())
                .countReported(r.getCountReported())
                .conditionNotes(r.getConditionNotes())
                .photos(r.getPhotos() != null ? List.of(r.getPhotos().split("\\|")) : List.of())
                .submittedBy(r.getSubmittedBy())
                .status(r.getStatus() != null ? r.getStatus().name() : null)
                .approvalDeadline(r.getApprovalDeadline() != null ? r.getApprovalDeadline().toString() : null)
                .approvedAt(r.getApprovedAt() != null ? r.getApprovedAt().toString() : null)
                .approvedBy(r.getApprovedBy())
                .build();
    }
}
