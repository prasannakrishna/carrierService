package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * Volumetric measurement for a shipment/RTS — captures actual dimensions and calculated weights.
 *
 * Industry formula:
 *   volumetricWeight = (length_cm × width_cm × height_cm) / divisor
 *   chargeableWeight = MAX(actualWeight, volumetricWeight)
 *
 * This determines whether shipment is charged by actual weight or volume.
 */
@Entity @Table(name = "shipment_volumetrics")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ShipmentVolumetrics {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rts_id", length = 100)
    private String rtsId;

    @Column(name = "shipment_id", length = 100)
    private String shipmentId;

    // ── Actual measurements ──
    @Column(name = "total_packages")
    private Integer totalPackages;

    @Column(name = "weight_unit", length = 10)
    private String weightUnit; // KG, LB

    @Column(name = "gross_weight", precision = 12, scale = 3)
    private BigDecimal grossWeight;

    @Column(name = "net_weight", precision = 12, scale = 3)
    private BigDecimal netWeight;

    @Column(name = "volume_unit", length = 10)
    private String volumeUnit; // M3, CFT

    @Column(name = "gross_volume", precision = 12, scale = 4)
    private BigDecimal grossVolume; // Actual volume occupied

    @Column(name = "dimension_unit", length = 10)
    private String dimensionUnit; // CM, M, IN

    @Column(name = "total_length", precision = 10, scale = 3)
    private BigDecimal totalLength;

    @Column(name = "total_width", precision = 10, scale = 3)
    private BigDecimal totalWidth;

    @Column(name = "total_height", precision = 10, scale = 3)
    private BigDecimal totalHeight;

    // ── Calculated fields ──
    @Column(name = "volumetric_weight", precision = 12, scale = 3)
    private BigDecimal volumetricWeight; // (L×W×H) / divisor

    @Column(name = "chargeable_weight", precision = 12, scale = 3)
    private BigDecimal chargeableWeight; // MAX(grossWeight, volumetricWeight)

    @Column(name = "volume_weight_ratio", precision = 6, scale = 3)
    private BigDecimal volumeWeightRatio; // volume/weight — indicates density

    // ── Packing info ──
    @Column(name = "pack_method", length = 30)
    private String packMethod; // PALLET, CARTON, BALE, ROLL, LOOSE

    @Column(name = "pallet_count")
    private Integer palletCount;

    @Column(name = "stackable")
    private Boolean stackable;
}
