package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "facility_operator_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityOperatorAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "operator_id", nullable = false)
    private UUID operatorId;

    @Column(name = "facility_id", nullable = false)
    private UUID facilityId;

    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;

    @Column(name = "shift_start", nullable = false)
    private Instant shiftStart;

    @Column(name = "shift_end", nullable = false)
    private Instant shiftEnd;

    @Column(name = "assigned_by", nullable = false)
    private UUID assignedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AssignmentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public enum AssignmentStatus {
        ACTIVE,
        COMPLETED,
        CANCELLED
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = AssignmentStatus.ACTIVE;
    }
}
