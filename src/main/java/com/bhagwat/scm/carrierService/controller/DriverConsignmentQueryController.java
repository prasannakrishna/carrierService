package com.bhagwat.scm.carrierService.controller;

import com.bhagwat.scm.carrierService.dto.DeliveryReportDto;
import com.bhagwat.scm.carrierService.service.DriverAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Remaining carrier-driver-app reads that live outside both /api/v1/driver/*
 * and /api/v1/transport/*: verification rules and delivery-report lookup.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Driver App — Consignment lookups")
public class DriverConsignmentQueryController {

    private final DriverAppService driverAppService;

    @GetMapping("/api/v1/consignments/{consignmentId}/verification-rules")
    @Operation(summary = "Rule-driven quality-check requirements for a consignment")
    public ResponseEntity<Map<String, Object>> getVerificationRules(@PathVariable String consignmentId) {
        return ResponseEntity.ok(driverAppService.getVerificationRules(consignmentId));
    }

    @GetMapping("/api/v1/delivery-reports/consignment/{consignmentId}")
    @Operation(summary = "Get the delivery report submitted for a consignment")
    public ResponseEntity<DeliveryReportDto> getDeliveryReport(@PathVariable String consignmentId) {
        return ResponseEntity.ok(driverAppService.getDeliveryReportByConsignment(consignmentId));
    }
}
