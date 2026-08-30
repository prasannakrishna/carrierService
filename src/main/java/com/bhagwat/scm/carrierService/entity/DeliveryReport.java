package com.bhagwat.scm.carrierService.entity;

import com.bhagwat.scm.carrierService.enums.DeliveryReportStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "delivery_reports")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DeliveryReport {
    @Id @Column(name = "report_id", nullable = false, updatable = false)
    private String reportId;

    @Column(name = "consignment_id", nullable = false, length = 100)
    private String consignmentId;

    @Column(name = "count_reported")
    private Integer countReported;

    @Column(name = "condition_notes", columnDefinition = "TEXT")
    private String conditionNotes;

    /** Photo URIs, stored pipe-delimited (kept simple — no JSON column needed for a short list). */
    @Column(name = "photos", columnDefinition = "TEXT")
    private String photos;

    @Column(name = "submitted_by", length = 100)
    private String submittedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private DeliveryReportStatus status = DeliveryReportStatus.PENDING_APPROVAL;

    @Column(name = "approval_deadline")
    private LocalDateTime approvalDeadline;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (reportId == null) reportId = UUID.randomUUID().toString();
        createdAt = LocalDateTime.now();
        if (status == null) status = DeliveryReportStatus.PENDING_APPROVAL;
        if (approvalDeadline == null) approvalDeadline = createdAt.plusHours(24);
    }
}
