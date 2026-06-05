package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Fleet — a logical grouping of vehicles and drivers within a carrier (tenant).
 * 
 * Hierarchy: Carrier (tenant) → Fleet → Vehicles + Drivers
 * 
 * A fleet typically represents a region, a vehicle type group, or a service type.
 * Transport orders can be assigned to a fleet, and the fleet manager allocates
 * specific vehicles/drivers from within the fleet.
 */
@Entity @Table(name = "fleets")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Fleet {
    @Id @Column(name = "fleet_id", length = 50)
    private String fleetId;

    @Column(name = "fleet_name", nullable = false, length = 200)
    private String fleetName;

    /** Fleet manager name or user ID */
    @Column(name = "manager", length = 200)
    private String manager;

    /** Number of vehicles assigned (can be computed or cached) */
    @Column(name = "vehicle_count")
    @Builder.Default
    private Integer vehicleCount = 0;

    /** Number of drivers assigned */
    @Column(name = "driver_count")
    @Builder.Default
    private Integer driverCount = 0;

    /** Operating region/zone for this fleet */
    @Column(name = "region", length = 100)
    private String region;

    /** BASE_CITY from which this fleet operates */
    @Column(name = "base_city", length = 100)
    private String baseCity;

    /** Fleet type: GENERAL, COLD_CHAIN, HEAVY, LAST_MILE, EXPRESS */
    @Column(name = "fleet_type", length = 30)
    @Builder.Default
    private String fleetType = "GENERAL";

    @Column(name = "status", length = 30)
    @Builder.Default
    private String status = "Active";

    @Column(name = "created_at")
    private Instant createdAt;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "custom_data", columnDefinition = "jsonb")
    private java.util.Map<String, Object> customData;

    @PrePersist
    void pre() {
        if (fleetId == null) fleetId = UUID.randomUUID().toString();
        createdAt = Instant.now();
    }
}
