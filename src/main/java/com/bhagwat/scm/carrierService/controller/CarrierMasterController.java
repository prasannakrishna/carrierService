package com.bhagwat.scm.carrierService.controller;

import com.bhagwat.scm.carrierService.dto.*;
import com.bhagwat.scm.carrierService.service.CarrierMasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/carrier/carriers")
@RequiredArgsConstructor
@Tag(name = "Carrier Master", description = "Manage carrier profiles and vehicles")
public class CarrierMasterController {

    private final CarrierMasterService carrierMasterService;

    @PostMapping
    @Operation(summary = "Register a new carrier")
    public ResponseEntity<CarrierResponse> createCarrier(@Valid @RequestBody CarrierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carrierMasterService.createCarrier(request));
    }

    @GetMapping
    @Operation(summary = "List all carriers")
    public ResponseEntity<List<CarrierResponse>> listCarriers() {
        return ResponseEntity.ok(carrierMasterService.listCarriers());
    }

    @GetMapping("/{carrierId}")
    @Operation(summary = "Get carrier by ID")
    public ResponseEntity<CarrierResponse> getCarrier(@PathVariable String carrierId) {
        return ResponseEntity.ok(carrierMasterService.getCarrier(carrierId));
    }

    @PutMapping("/{carrierId}")
    @Operation(summary = "Update carrier profile")
    public ResponseEntity<CarrierResponse> updateCarrier(@PathVariable String carrierId,
                                                          @Valid @RequestBody CarrierRequest request) {
        return ResponseEntity.ok(carrierMasterService.updateCarrier(carrierId, request));
    }

    // ── Vehicle endpoints ────────────────────────────────────────────────────

    @PostMapping("/vehicles")
    @Operation(summary = "Add vehicle to carrier fleet")
    public ResponseEntity<CarrierVehicleResponse> addVehicle(@Valid @RequestBody CarrierVehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carrierMasterService.addVehicle(request));
    }

    @GetMapping("/vehicles")
    @Operation(summary = "List all vehicles across all carriers")
    public ResponseEntity<List<CarrierVehicleResponse>> listAllVehicles() {
        return ResponseEntity.ok(carrierMasterService.getAllVehicles());
    }

    @GetMapping("/{carrierId}/vehicles")
    @Operation(summary = "List vehicles for a carrier")
    public ResponseEntity<List<CarrierVehicleResponse>> listVehicles(@PathVariable String carrierId) {
        return ResponseEntity.ok(carrierMasterService.getVehicles(carrierId));
    }

    @GetMapping("/vehicles/{vehicleId}")
    @Operation(summary = "Get vehicle by ID")
    public ResponseEntity<CarrierVehicleResponse> getVehicle(@PathVariable String vehicleId) {
        return ResponseEntity.ok(carrierMasterService.getVehicleById(vehicleId));
    }

    @PutMapping("/vehicles/{vehicleId}")
    @Operation(summary = "Update vehicle details")
    public ResponseEntity<CarrierVehicleResponse> updateVehicle(@PathVariable String vehicleId,
                                                                  @Valid @RequestBody CarrierVehicleRequest request) {
        return ResponseEntity.ok(carrierMasterService.updateVehicle(vehicleId, request));
    }

    @DeleteMapping("/vehicles/{vehicleId}")
    @Operation(summary = "Delete vehicle")
    public ResponseEntity<Void> deleteVehicle(@PathVariable String vehicleId) {
        carrierMasterService.deleteVehicle(vehicleId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/vehicles/{vehicleId}/status")
    @Operation(summary = "Update vehicle status")
    public ResponseEntity<CarrierVehicleResponse> updateVehicleStatus(@PathVariable String vehicleId,
                                                                        @RequestBody VehicleStatusRequest request) {
        return ResponseEntity.ok(carrierMasterService.updateVehicleStatus(vehicleId, request.getStatus()));
    }

    // ── Assignment Operations ────────────────────────────────────────────────

    @PatchMapping("/vehicles/{vehicleId}/assign-fleet")
    @Operation(summary = "Assign vehicle to a fleet")
    public ResponseEntity<CarrierVehicleResponse> assignVehicleToFleet(
            @PathVariable String vehicleId, @RequestParam String fleetId) {
        return ResponseEntity.ok(carrierMasterService.assignVehicleToFleet(vehicleId, fleetId));
    }

    @PatchMapping("/vehicles/{vehicleId}/unassign-fleet")
    @Operation(summary = "Unassign vehicle from fleet (blocked if active shipment)")
    public ResponseEntity<CarrierVehicleResponse> unassignVehicleFromFleet(@PathVariable String vehicleId) {
        return ResponseEntity.ok(carrierMasterService.unassignVehicleFromFleet(vehicleId));
    }

    @PatchMapping("/vehicles/{vehicleId}/assign-driver")
    @Operation(summary = "Assign driver to vehicle")
    public ResponseEntity<CarrierVehicleResponse> assignDriverToVehicle(
            @PathVariable String vehicleId, @RequestParam String driverId) {
        return ResponseEntity.ok(carrierMasterService.assignDriverToVehicle(vehicleId, driverId));
    }

    @PatchMapping("/vehicles/{vehicleId}/unassign-driver")
    @Operation(summary = "Unassign driver from vehicle (blocked if active shipment)")
    public ResponseEntity<CarrierVehicleResponse> unassignDriverFromVehicle(@PathVariable String vehicleId) {
        return ResponseEntity.ok(carrierMasterService.unassignDriverFromVehicle(vehicleId));
    }
}
