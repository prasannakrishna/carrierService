package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain-specific workers/operators who use mobile apps.
 * These are NOT platform users (userService) — they are local to this service.
 * 
 * Examples in carrier domain: Driver, Dispatcher, Helper
 * Auth: PIN-based login via this service's own auth endpoint.
 */
@Entity @Table(name = "domain_workers")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DomainWorker {
    @Id @Column(name = "worker_id", length = 50)
    private String workerId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "email", length = 200)
    private String email;

    /** FK to DomainRole */
    @Column(name = "role_id", nullable = false, length = 50)
    private String roleId;

    /** 4-6 digit PIN for mobile app login */
    @Column(name = "pin", length = 10)
    private String pin;

    /** If this worker also has a platform (web) account, link it */
    @Column(name = "platform_user_id", length = 100)
    private String platformUserId;

    /** Reference to driver entity if this worker is a driver */
    @Column(name = "driver_id", length = 50)
    private String driverId;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "device_token", length = 500)
    private String deviceToken;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void pre() {
        if (workerId == null) workerId = "WRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUp() { updatedAt = LocalDateTime.now(); }
}
