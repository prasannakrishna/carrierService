package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * An exception raised by a driver from carrier-driver-app during transit
 * (damage, mismatch, access issue, etc.). Distinct from the pre-existing
 * {@link TransportException} entity, which backs the logistics-ops exception
 * queue (LogisticsOpsController) and has a different, unrelated shape.
 */
@Entity @Table(name = "driver_field_exceptions")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DriverFieldException {
    @Id @GeneratedValue @Column(name = "field_exception_id")
    private UUID fieldExceptionId;

    @Column(name = "ts_id", nullable = false, length = 100)
    private String tsId;

    @Column(name = "consignment_id", length = 100)
    private String consignmentId;

    @Column(name = "exception_type", nullable = false, length = 50)
    private String exceptionType;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "photos", columnDefinition = "TEXT")
    private String photos;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "raised_by", length = 100)
    private String raisedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
