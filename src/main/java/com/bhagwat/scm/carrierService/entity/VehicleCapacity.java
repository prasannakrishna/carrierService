package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * Vehicle capacity master — defines volumetric limits per vehicle type.
 * Based on industry standards (ISO container sizes, truck body dimensions).
 *
 * Volumetric weight formula: (L × W × H) / divisor
 * Standard divisors: Air=5000, Road=4000, Sea=1000
 */
@Entity @Table(name = "vehicle_capacities")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class VehicleCapacity {

    @Id @Column(name = "capacity_id", length = 50)
    private String capacityId;

    @Column(name = "vehicle_type", nullable = false, length = 50)
    private String vehicleType; // TRUCK_14FT, TRUCK_20FT, VAN, LORRY, CONTAINER_20FT, CONTAINER_40FT

    @Column(name = "vehicle_sub_type", length = 50)
    private String vehicleSubType; // OPEN, CLOSED, REFRIGERATED, FLATBED

    // ── Weight capacity ──
    @Column(name = "max_payload_kg", precision = 12, scale = 2)
    private BigDecimal maxPayloadKg;

    @Column(name = "tare_weight_kg", precision = 12, scale = 2)
    private BigDecimal tareWeightKg; // Empty vehicle weight

    @Column(name = "gross_vehicle_weight_kg", precision = 12, scale = 2)
    private BigDecimal grossVehicleWeightKg; // Max total (payload + tare)

    // ── Volume capacity (internal cargo space) ──
    @Column(name = "internal_length_m", precision = 8, scale = 3)
    private BigDecimal internalLengthM;

    @Column(name = "internal_width_m", precision = 8, scale = 3)
    private BigDecimal internalWidthM;

    @Column(name = "internal_height_m", precision = 8, scale = 3)
    private BigDecimal internalHeightM;

    @Column(name = "max_volume_m3", precision = 10, scale = 4)
    private BigDecimal maxVolumeM3; // L × W × H (usable cargo volume)

    // ── Volumetric weight divisor ──
    @Column(name = "volumetric_divisor")
    private Integer volumetricDivisor; // 4000 for road, 5000 for air, 1000 for sea

    // ── Pallet/package capacity ──
    @Column(name = "max_pallets")
    private Integer maxPallets; // Standard EUR pallets (1.2m × 0.8m)

    @Column(name = "max_packages")
    private Integer maxPackages;

    // ── Door dimensions (loading constraints) ──
    @Column(name = "door_width_m", precision = 6, scale = 3)
    private BigDecimal doorWidthM;

    @Column(name = "door_height_m", precision = 6, scale = 3)
    private BigDecimal doorHeightM;

    // ── Utilization factor (practical vs theoretical) ──
    @Column(name = "utilization_factor_pct", precision = 5, scale = 2)
    private BigDecimal utilizationFactorPct; // Typically 80-85% (stacking gaps, irregular shapes)

    @Column(name = "status", length = 20)
    private String status;
}
