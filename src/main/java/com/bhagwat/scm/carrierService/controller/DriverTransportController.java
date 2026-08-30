package com.bhagwat.scm.carrierService.controller;

import com.bhagwat.scm.carrierService.dto.ConsignmentDto;
import com.bhagwat.scm.carrierService.dto.DriverShipmentDto;
import com.bhagwat.scm.carrierService.dto.TransportExceptionRequest;
import com.bhagwat.scm.carrierService.service.DriverAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * carrier-driver-app also calls a handful of endpoints under /api/v1/transport/*
 * (shipment/consignment reads, exception raising) distinct from its own
 * /api/v1/driver/* namespace — kept in a separate controller to match the app's
 * actual base paths rather than folding everything under one prefix.
 */
@RestController
@RequestMapping("/api/v1/transport/shipments")
@RequiredArgsConstructor
@Tag(name = "Driver App — Transport", description = "Shipment/consignment reads and exceptions for carrier-driver-app")
public class DriverTransportController {

    private final DriverAppService driverAppService;

    @GetMapping("/{tsId}")
    @Operation(summary = "Get shipment detail (driver-app shape)")
    public ResponseEntity<DriverShipmentDto> getShipmentDetail(@PathVariable String tsId) {
        return ResponseEntity.ok(driverAppService.getShipmentDetail(tsId));
    }

    @GetMapping("/{tsId}/consignments")
    @Operation(summary = "List consignments on this shipment")
    public ResponseEntity<List<ConsignmentDto>> getConsignments(@PathVariable String tsId) {
        return ResponseEntity.ok(driverAppService.getConsignments(tsId));
    }

    @PostMapping("/{tsId}/exceptions")
    @Operation(summary = "Raise a transport exception from the field")
    public ResponseEntity<Map<String, Object>> raiseException(
            @PathVariable String tsId,
            @Valid @RequestBody TransportExceptionRequest request,
            @RequestHeader(value = "X-Driver-Id", required = false) String driverId) {
        driverAppService.raiseException(tsId, request, driverId);
        return ResponseEntity.ok(Map.of("status", "RECORDED", "tsId", tsId));
    }
}
