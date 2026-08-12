package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "domain_operator_activity_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainOperatorActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "activity_id")
    private UUID activityId;

    @Column(name = "operator_id", nullable = false)
    private UUID operatorId;

    @Column(name = "facility_id", nullable = false)
    private UUID facilityId;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_metadata", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> actionMetadata;

    @Column(name = "outcome", nullable = false, length = 10)
    @Builder.Default
    private String outcome = "SUCCESS";

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "performed_at", nullable = false)
    private Instant performedAt;

    @PrePersist
    void prePersist() {
        if (performedAt == null) performedAt = Instant.now();
        if (outcome == null) outcome = "SUCCESS";
    }
}
