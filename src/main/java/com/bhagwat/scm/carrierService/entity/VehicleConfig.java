package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name = "vehicle_configs")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class VehicleConfig {
    @Id @Column(name = "config_id", length = 50)
    private String configId;
    @Column(name = "vehicle_type", length = 50)
    private String vehicleType;
    @Column(name = "max_load_kg", precision = 12, scale = 2)
    private BigDecimal maxLoadKg;
    @Column(name = "dimensions", length = 100)
    private String dimensions;
    @Column(name = "fuel_type", length = 30)
    private String fuelType;
    @Column(name = "status", length = 30)
    private String status;
}
