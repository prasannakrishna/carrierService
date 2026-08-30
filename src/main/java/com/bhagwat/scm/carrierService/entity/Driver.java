package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "drivers")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Driver {
    @Id @Column(name = "driver_id", length = 50)
    private String driverId;
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    @Column(name = "license_no", unique = true, length = 50)
    private String licenseNo;
    @Column(name = "contact", length = 20)
    private String contact;
    @Column(name = "fleet_id", length = 50)
    private String fleetId;
    @Column(name = "status", length = 30)
    private String status;
    @Column(name = "pin_hash", length = 255)
    private String pinHash;
    @Column(name = "created_at")
    private Instant createdAt;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @jakarta.persistence.Column(name = "custom_data", columnDefinition = "jsonb")
    private java.util.Map<String, Object> customData;
    @PrePersist void pre() { createdAt = Instant.now(); }
}
