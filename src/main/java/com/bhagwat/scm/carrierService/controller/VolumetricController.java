package com.bhagwat.scm.carrierService.controller;

import com.bhagwat.scm.carrierService.entity.ShipmentVolumetrics;
import com.bhagwat.scm.carrierService.entity.VehicleCapacity;
import com.bhagwat.scm.carrierService.repository.VehicleCapacityRepository;
import com.bhagwat.scm.carrierService.service.VolumetricService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/carrier/volumetrics")
@RequiredArgsConstructor
public class VolumetricController {

    private final VolumetricService volumetricService;
    private final VehicleCapacityRepository capacityRepo;

    // ── Vehicle Capacity Master ──

    @PostMapping("/vehicle-capacities")
    public ResponseEntity<VehicleCapacity> createCapacity(@RequestBody VehicleCapacity vc) {
        if (vc.getCapacityId() == null) vc.setCapacityId("VC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        // Auto-calculate max volume if dimensions provided
        if (vc.getMaxVolumeM3() == null && vc.getInternalLengthM() != null) {
            vc.setMaxVolumeM3(vc.getInternalLengthM().multiply(vc.getInternalWidthM()).multiply(vc.getInternalHeightM()));
        }
        return ResponseEntity.ok(capacityRepo.save(vc));
    }

    @GetMapping("/vehicle-capacities")
    public ResponseEntity<List<VehicleCapacity>> getCapacities() {
        return ResponseEntity.ok(capacityRepo.findAll());
    }

    // ── Shipment Volumetric Calculation ──

    @PostMapping("/calculate")
    public ResponseEntity<ShipmentVolumetrics> calculate(@RequestBody VolumetricRequest req) {
        return ResponseEntity.ok(volumetricService.calculate(
                req.getRtsId(), req.getLengthCm(), req.getWidthCm(), req.getHeightCm(),
                req.getActualWeightKg(), req.getPackages(), req.getPackMethod(), req.isStackable()));
    }

    // ── Vehicle Matching ──

    @PostMapping("/find-vehicle")
    public ResponseEntity<VolumetricService.VehicleMatchResult> findVehicle(@RequestBody VehicleMatchRequest req) {
        return ResponseEntity.ok(volumetricService.findBestVehicle(
                req.getWeightKg(), req.getVolumeM3(), req.getPallets()));
    }

    // ── Capacity Check (can this fit in existing plan?) ──

    @PostMapping("/can-fit")
    public ResponseEntity<Boolean> canFit(@RequestBody CapacityCheckRequest req) {
        return ResponseEntity.ok(volumetricService.canFitInPlan(
                req.getVehicleType(), req.getCurrentWeightKg(), req.getCurrentVolumeM3(),
                req.getAdditionalWeightKg(), req.getAdditionalVolumeM3()));
    }

    @Data
    public static class VolumetricRequest {
        private String rtsId;
        private BigDecimal lengthCm;
        private BigDecimal widthCm;
        private BigDecimal heightCm;
        private BigDecimal actualWeightKg;
        private int packages;
        private String packMethod;
        private boolean stackable;
    }

    @Data
    public static class VehicleMatchRequest {
        private BigDecimal weightKg;
        private BigDecimal volumeM3;
        private int pallets;
    }

    @Data
    public static class CapacityCheckRequest {
        private String vehicleType;
        private BigDecimal currentWeightKg;
        private BigDecimal currentVolumeM3;
        private BigDecimal additionalWeightKg;
        private BigDecimal additionalVolumeM3;
    }
}
