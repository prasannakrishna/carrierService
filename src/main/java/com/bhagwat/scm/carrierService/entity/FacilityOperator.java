package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "facility_operators")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityOperator {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owning_service", nullable = false, length = 20)
    private String owningService;

    @Column(name = "facility_owner_org_id", nullable = false)
    private UUID facilityOwnerOrgId;

    @Column(name = "operator_name", nullable = false, length = 128)
    private String operatorName;

    @Column(name = "pin_hash", nullable = false)
    private String pinHash;

    @Column(name = "pin_last_changed_at", nullable = false)
    private Instant pinLastChangedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OperatorStatus status;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "vehicle_type", length = 50)
    private String vehicleType;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum OperatorStatus {
        ACTIVE,
        SUSPENDED,
        DEACTIVATED,
        PIN_RESET_REQUIRED,
        PENDING_VERIFICATION
    }
}
