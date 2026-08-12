package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "task_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "operator_id", nullable = false)
    private UUID operatorId;

    @Column(name = "action", nullable = false, length = 30)
    private String action; // CLAIMED, COMPLETED, RELEASED, TIMEOUT_RELEASED, FORCE_RELEASED

    @Column(name = "previous_lock_operator_id")
    private UUID previousLockOperatorId;

    @Column(name = "performed_at", nullable = false)
    private Instant performedAt;

    @PrePersist
    void prePersist() {
        if (performedAt == null) performedAt = Instant.now();
    }
}
