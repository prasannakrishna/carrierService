package com.bhagwat.scm.carrierService.entity;

import com.bhagwat.scm.carrierService.enums.CarrierType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Defines which geographic areas a carrier can service.
 *
 * A carrier registers the clusters (3-digit pincode prefixes) they cover,
 * along with their service type (FIRST_MILE, MID_MILE, LAST_MILE).
 *
 * This enables the logistics resolver to find:
 *   - Last mile providers for a demand cluster
 *   - First mile providers for a supply cluster
 *   - Line-haul carriers between two clusters
 *
 * Example:
 *   Carrier "BlueDart"  → LAST_MILE  → clusters: 560, 561, 562 (Bangalore area)
 *   Carrier "Delhivery"  → FIRST_MILE → clusters: 560, 600, 400 (multi-city pickup)
 *   Carrier "VRL Logistics" → MID_MILE → origin: 560, destination: 600 (Bangalore→Chennai line-haul)
 */
@Entity
@Table(name = "carrier_service_areas",
        indexes = {
                @Index(name = "idx_csa_carrier", columnList = "carrier_id"),
                @Index(name = "idx_csa_cluster_type", columnList = "cluster_prefix, service_type"),
                @Index(name = "idx_csa_service_type", columnList = "service_type")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarrierServiceArea {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "carrier_id", nullable = false, length = 100)
    private String carrierId;

    @Column(name = "carrier_name", length = 255)
    private String carrierName;

    /**
     * Service type determines what leg this carrier handles:
     *   LAST_MILE  → picks from hub/store, delivers to customer doorstep
     *   FIRST_MILE → picks from seller/warehouse, delivers to hub
     *   MID_MILE   → inter-city/inter-cluster line-haul transport
     *   FULL_SERVICE → end-to-end (pickup to delivery)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 20)
    private ServiceType serviceType;

    /** 3-digit pincode prefix this carrier services (for LAST_MILE and FIRST_MILE) */
    @Column(name = "cluster_prefix", length = 3)
    private String clusterPrefix;

    /** For MID_MILE: origin cluster */
    @Column(name = "origin_cluster", length = 3)
    private String originCluster;

    /** For MID_MILE: destination cluster */
    @Column(name = "destination_cluster", length = 3)
    private String destinationCluster;

    /** City covered (optional, for display) */
    @Column(name = "city", length = 100)
    private String city;

    /** State covered */
    @Column(name = "state", length = 100)
    private String state;

    /** Rate per kg for this service area */
    @Column(name = "rate_per_kg", precision = 10, scale = 2)
    private BigDecimal ratePerKg;

    /** Flat rate per shipment */
    @Column(name = "flat_rate", precision = 10, scale = 2)
    private BigDecimal flatRate;

    /** Estimated delivery days for this service area */
    @Column(name = "estimated_days")
    private Integer estimatedDays;

    /** Maximum weight this carrier handles (kg) */
    @Column(name = "max_weight_kg", precision = 10, scale = 2)
    private BigDecimal maxWeightKg;

    /** Is this service area currently active? */
    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID().toString();
        createdAt = updatedAt = LocalDateTime.now();
        if (active == null) active = true;
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public enum ServiceType {
        LAST_MILE,      // Hub/Store → Customer doorstep
        FIRST_MILE,     // Seller/Warehouse → Hub
        MID_MILE,       // Hub → Hub (inter-city line-haul)
        FULL_SERVICE    // End-to-end (pickup → delivery)
    }
}
