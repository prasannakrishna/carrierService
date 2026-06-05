package com.bhagwat.scm.carrierService.controller;

import com.bhagwat.scm.carrierService.dto.*;
import com.bhagwat.scm.carrierService.enums.TransportShipmentStatus;
import com.bhagwat.scm.carrierService.service.TransportShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/carrier/shipments")
@RequiredArgsConstructor
@Tag(name = "Transport Shipment", description = "Transport Shipment tracking and milestone management")
public class TransportShipmentController {

    private final TransportShipmentService shipmentService;

    @GetMapping("/{tsId}")
    @Operation(summary = "Get Transport Shipment by ID (includes milestones)")
    public ResponseEntity<TransportShipmentResponse> getShipment(@PathVariable String tsId) {
        return ResponseEntity.ok(shipmentService.getShipment(tsId));
    }

    @GetMapping
    @Operation(summary = "List shipments by carrier (optionally filtered by status)")
    public ResponseEntity<List<TransportShipmentResponse>> listShipments(
            @RequestParam String carrierId,
            @RequestParam(required = false) TransportShipmentStatus status) {
        if (status != null) {
            return ResponseEntity.ok(shipmentService.listByCarrierAndStatus(carrierId, status));
        }
        return ResponseEntity.ok(shipmentService.listByCarrier(carrierId));
    }

    @PostMapping("/milestones")
    @Operation(summary = "Post a shipment milestone (auto-advances shipment status)")
    public ResponseEntity<MilestoneResponse> postMilestone(@Valid @RequestBody MilestoneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shipmentService.postMilestone(request));
    }

    @PatchMapping("/{tsId}/assign-vehicle")
    @Operation(summary = "Assign vehicle and driver to a shipment")
    public ResponseEntity<TransportShipmentResponse> assignVehicle(
            @PathVariable String tsId,
            @RequestParam String vehicleId,
            @RequestParam String vehicleNumber,
            @RequestParam(required = false) String driverName,
            @RequestParam(required = false) String driverPhone) {
        return ResponseEntity.ok(shipmentService.assignVehicle(tsId, vehicleId, vehicleNumber, driverName, driverPhone));
    }

    @PatchMapping("/{tsId}/location")
    @Operation(summary = "Update current location of shipment")
    public ResponseEntity<TransportShipmentResponse> updateLocation(@PathVariable String tsId,
                                                                     @RequestBody UpdateLocationRequest request) {
        return ResponseEntity.ok(shipmentService.updateLocation(tsId, request.getLocation()));
    }

    @PatchMapping("/{tsId}/assign-plan")
    @Operation(summary = "Assign a transport plan to this shipment")
    public ResponseEntity<Void> assignPlan(@PathVariable String tsId,
                                           @RequestBody AssignPlanRequest request) {
        shipmentService.assignTransportPlanByRtsId(tsId, request.getTransportPlanId(), request.getTransportPlanNumber());
        return ResponseEntity.noContent().build();
    }
}
