package com.bhagwat.scm.carrierService.controller;

import com.bhagwat.scm.carrierService.dto.*;
import com.bhagwat.scm.carrierService.service.DriverAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Backend for carrier-driver-app (Android). Paths and payload shapes here are
 * dictated by the app's Retrofit interface (CarrierDriverApi.kt) — this
 * controller exists to match that contract, not the other way round.
 */
@RestController
@RequestMapping("/api/v1/driver")
@RequiredArgsConstructor
@Tag(name = "Driver App", description = "Endpoints consumed by carrier-driver-app")
public class DriverController {

    private final DriverAppService driverAppService;

    @PostMapping("/auth/login")
    @Operation(summary = "Driver login by vehicle number + PIN")
    public ResponseEntity<DriverSessionResponse> login(@Valid @RequestBody DriverLoginRequest request) {
        return ResponseEntity.ok(driverAppService.login(request));
    }

    @GetMapping("/shipments/active")
    @Operation(summary = "Active transport shipments assigned to this vehicle")
    public ResponseEntity<List<DriverShipmentDto>> getActiveShipments(
            @RequestHeader("X-Vehicle-Id") String vehicleId) {
        return ResponseEntity.ok(driverAppService.getActiveShipments(vehicleId));
    }

    @PostMapping("/shipments/{tsId}/scan-load")
    @Operation(summary = "Scan a consignment barcode to mark it loaded")
    public ResponseEntity<ConsignmentLoadResult> scanLoad(
            @PathVariable String tsId, @Valid @RequestBody ScanLoadRequest request) {
        return ResponseEntity.ok(driverAppService.scanLoad(tsId, request.getConsignmentBarcode()));
    }

    @PostMapping("/shipments/{tsId}/mark-loaded")
    @Operation(summary = "Confirm the vehicle is fully loaded (all consignments scanned)")
    public ResponseEntity<DriverShipmentDto> markLoaded(@PathVariable String tsId) {
        return ResponseEntity.ok(driverAppService.markLoaded(tsId));
    }

    @PostMapping("/shipments/{tsId}/milestones")
    @Operation(summary = "Post a transit milestone for this shipment")
    public ResponseEntity<DriverMilestoneResponse> postMilestone(
            @PathVariable String tsId,
            @Valid @RequestBody DriverMilestoneRequest request,
            @RequestHeader(value = "X-Driver-Id", required = false) String driverId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(driverAppService.postMilestone(tsId, request, driverId));
    }

    @PostMapping("/shipments/{tsId}/arrive/{stopId}")
    @Operation(summary = "Mark arrival at a stop")
    public ResponseEntity<DriverShipmentDto> markArrived(
            @PathVariable String tsId, @PathVariable String stopId) {
        return ResponseEntity.ok(driverAppService.arriveAtStop(tsId, stopId));
    }

    @PostMapping("/delivery-reports")
    @Operation(summary = "Submit a delivery report (mutual attestation)")
    public ResponseEntity<DeliveryReportDto> submitDeliveryReport(
            @Valid @RequestBody DeliveryReportRequestDto request,
            @RequestHeader(value = "X-Driver-Id", required = false) String driverId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(driverAppService.submitDeliveryReport(request, driverId));
    }

    @PostMapping("/consignments/{consignmentId}/confirm-unload")
    @Operation(summary = "Confirm a consignment has been unloaded")
    public ResponseEntity<ConsignmentDto> confirmUnload(@PathVariable String consignmentId) {
        return ResponseEntity.ok(driverAppService.confirmUnload(consignmentId));
    }
}
