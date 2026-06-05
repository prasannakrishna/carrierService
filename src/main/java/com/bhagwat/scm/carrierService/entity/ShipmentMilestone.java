package com.bhagwat.scm.carrierService.entity;

import com.bhagwat.scm.carrierService.enums.MilestoneType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "shipment_milestones")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ShipmentMilestone {
    @Id @Column(name = "milestone_id", nullable = false, updatable = false)
    private String milestoneId;

    @Column(name = "ts_id", nullable = false, length = 100)
    private String tsId;

    @Enumerated(EnumType.STRING)
    @Column(name = "milestone_type", nullable = false, length = 30)
    private MilestoneType milestoneType;

    @Column(name = "milestone_date_time", nullable = false)
    private LocalDateTime milestoneDateTime;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "latitude", precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "posted_by", length = 255)
    private String postedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (milestoneId == null) milestoneId = UUID.randomUUID().toString();
        createdAt = LocalDateTime.now();
    }
}
