package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Configurable roles within this domain service.
 * roleType: OPERATOR (mobile app users) or MANAGER (supervisors, may also have web access)
 * roleName: free text — e.g. "Driver", "Dispatcher", "Fleet Supervisor"
 */
@Entity @Table(name = "domain_roles")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DomainRole {
    @Id @Column(name = "role_id", length = 50)
    private String roleId;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    /** OPERATOR or MANAGER */
    @Column(name = "role_type", nullable = false, length = 20)
    private String roleType;

    @Column(name = "description", length = 500)
    private String description;

    /** What mobile app features this role can access (JSON array) */
    @Column(name = "permissions", columnDefinition = "TEXT")
    private String permissions;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void pre() {
        if (roleId == null) roleId = "ROLE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        createdAt = LocalDateTime.now();
    }
}
