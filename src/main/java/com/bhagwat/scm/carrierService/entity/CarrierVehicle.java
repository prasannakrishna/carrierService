package com.bhagwat.scm.carrierService.entity;

import com.bhagwat.scm.carrierService.enums.VehicleStatus;
import com.bhagwat.scm.carrierService.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "carrier_vehicles")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CarrierVehicle {
    @Id @Column(name = "vehicle_id", nullable = false, updatable = false)
    private String vehicleId;

    @Column(name = "carrier_id", nullable = false, length = 100)
    private String carrierId;

    @Column(name = "fleet_id", length = 50)
    private String fleetId;

    @Column(name = "vehicle_number", nullable = false, length = 30)
    private String vehicleNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", length = 30)
    private VehicleType vehicleType;

    @Column(name = "capacity_kg", precision = 10, scale = 2)
    private BigDecimal capacityKg;

    @Column(name = "volume_capacity_cbm", precision = 10, scale = 3)
    private BigDecimal volumeCapacityCbm;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    /** Currently assigned driver (null = unassigned) */
    @Column(name = "driver_id", length = 50)
    private String driverId;

    @Column(name = "driver_name", length = 255)
    private String driverName;

    @Column(name = "driver_phone", length = 30)
    private String driverPhone;

    @Column(name = "driver_license", length = 50)
    private String driverLicense;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (vehicleId == null) vehicleId = UUID.randomUUID().toString();
        createdAt = updatedAt = LocalDateTime.now();
        if (status == null) status = VehicleStatus.AVAILABLE;
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
