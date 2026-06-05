package com.bhagwat.scm.carrierService.service;

import com.bhagwat.scm.carrierService.entity.ShipmentVolumetrics;
import com.bhagwat.scm.carrierService.entity.VehicleCapacity;
import com.bhagwat.scm.carrierService.repository.ShipmentVolumetricsRepository;
import com.bhagwat.scm.carrierService.repository.VehicleCapacityRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

/**
 * Volumetric calculation engine.
 *
 * Key formulas:
 *   volumetricWeight = (L_cm × W_cm × H_cm) / divisor
 *   chargeableWeight = MAX(actualWeight, volumetricWeight)
 *   vehicleUtilization% = (shipmentVolume / vehicleMaxVolume) × 100
 *
 * Vehicle selection: find smallest vehicle that fits both weight AND volume.
 */
@Service
@RequiredArgsConstructor
public class VolumetricService {

    private final VehicleCapacityRepository capacityRepo;
    private final ShipmentVolumetricsRepository volumetricsRepo;

    private static final int DEFAULT_ROAD_DIVISOR = 4000;

    /**
     * Calculate volumetric weight and chargeable weight for a shipment.
     */
    public ShipmentVolumetrics calculate(String rtsId, BigDecimal lengthCm, BigDecimal widthCm,
                                          BigDecimal heightCm, BigDecimal actualWeightKg,
                                          int packages, String packMethod, boolean stackable) {
        BigDecimal volumeM3 = lengthCm.multiply(widthCm).multiply(heightCm)
                .divide(BigDecimal.valueOf(1_000_000), 4, RoundingMode.HALF_UP);

        BigDecimal volumetricWeight = lengthCm.multiply(widthCm).multiply(heightCm)
                .divide(BigDecimal.valueOf(DEFAULT_ROAD_DIVISOR), 3, RoundingMode.HALF_UP);

        BigDecimal chargeableWeight = actualWeightKg.max(volumetricWeight);

        BigDecimal ratio = actualWeightKg.compareTo(BigDecimal.ZERO) > 0
                ? volumeM3.divide(actualWeightKg, 3, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        ShipmentVolumetrics vol = ShipmentVolumetrics.builder()
                .rtsId(rtsId)
                .totalPackages(packages)
                .weightUnit("KG").grossWeight(actualWeightKg)
                .volumeUnit("M3").grossVolume(volumeM3)
                .dimensionUnit("CM")
                .totalLength(lengthCm).totalWidth(widthCm).totalHeight(heightCm)
                .volumetricWeight(volumetricWeight)
                .chargeableWeight(chargeableWeight)
                .volumeWeightRatio(ratio)
                .packMethod(packMethod)
                .stackable(stackable)
                .build();

        return volumetricsRepo.save(vol);
    }

    /**
     * Find the best-fit vehicle for a given shipment's weight and volume.
     * Returns smallest vehicle that can carry the load.
     */
    public VehicleMatchResult findBestVehicle(BigDecimal weightKg, BigDecimal volumeM3, int pallets) {
        List<VehicleCapacity> vehicles = capacityRepo.findByStatus("Active");

        VehicleCapacity best = vehicles.stream()
                .filter(v -> v.getMaxPayloadKg().compareTo(weightKg) >= 0)
                .filter(v -> v.getMaxVolumeM3().compareTo(volumeM3) >= 0)
                .filter(v -> pallets <= 0 || (v.getMaxPallets() != null && v.getMaxPallets() >= pallets))
                .min(Comparator.comparing(VehicleCapacity::getMaxVolumeM3))
                .orElse(null);

        if (best == null) {
            return VehicleMatchResult.builder()
                    .matched(false)
                    .message("No single vehicle can carry " + weightKg + "kg / " + volumeM3 + "m³. Split required.")
                    .build();
        }

        BigDecimal usableVolume = best.getMaxVolumeM3()
                .multiply(best.getUtilizationFactorPct() != null
                        ? best.getUtilizationFactorPct().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                        : BigDecimal.valueOf(0.85));

        BigDecimal weightUtil = weightKg.divide(best.getMaxPayloadKg(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        BigDecimal volumeUtil = volumeM3.divide(usableVolume, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return VehicleMatchResult.builder()
                .matched(true)
                .vehicleType(best.getVehicleType())
                .vehicleSubType(best.getVehicleSubType())
                .maxPayloadKg(best.getMaxPayloadKg())
                .maxVolumeM3(best.getMaxVolumeM3())
                .usableVolumeM3(usableVolume)
                .weightUtilizationPct(weightUtil.setScale(1, RoundingMode.HALF_UP))
                .volumeUtilizationPct(volumeUtil.setScale(1, RoundingMode.HALF_UP))
                .remainingWeightKg(best.getMaxPayloadKg().subtract(weightKg))
                .remainingVolumeM3(usableVolume.subtract(volumeM3))
                .build();
    }

    /**
     * Check if an additional shipment can fit into an existing plan's vehicle.
     */
    public boolean canFitInPlan(String vehicleType, BigDecimal currentWeightKg, BigDecimal currentVolumeM3,
                                BigDecimal additionalWeightKg, BigDecimal additionalVolumeM3) {
        VehicleCapacity cap = capacityRepo.findByVehicleType(vehicleType);
        if (cap == null) return false;

        BigDecimal usableVolume = cap.getMaxVolumeM3()
                .multiply(BigDecimal.valueOf(0.85));

        boolean weightFits = currentWeightKg.add(additionalWeightKg).compareTo(cap.getMaxPayloadKg()) <= 0;
        boolean volumeFits = currentVolumeM3.add(additionalVolumeM3).compareTo(usableVolume) <= 0;

        return weightFits && volumeFits;
    }

    @Data @Builder
    public static class VehicleMatchResult {
        private boolean matched;
        private String vehicleType;
        private String vehicleSubType;
        private BigDecimal maxPayloadKg;
        private BigDecimal maxVolumeM3;
        private BigDecimal usableVolumeM3;
        private BigDecimal weightUtilizationPct;
        private BigDecimal volumeUtilizationPct;
        private BigDecimal remainingWeightKg;
        private BigDecimal remainingVolumeM3;
        private String message;
    }
}
