package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "issues")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Issue {
    @Id @Column(name = "issue_id", length = 50)
    private String issueId;
    @Column(name = "shipment_id", length = 50)
    private String shipmentId;
    @Column(name = "priority", length = 20)
    private String priority;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Column(name = "assigned_to", length = 100)
    private String assignedTo;
    @Column(name = "status", length = 30)
    private String status;
    @Column(name = "created_at")
    private Instant createdAt;
    @PrePersist void pre() { createdAt = Instant.now(); }
}
