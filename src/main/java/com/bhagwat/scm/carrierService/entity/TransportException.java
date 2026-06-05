package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "transport_exceptions")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransportException {
    @Id @Column(name = "exception_id", length = 50)
    private String exceptionId;
    @Column(name = "shipment_id", length = 50)
    private String shipmentId;
    @Column(name = "type", length = 50)
    private String type;
    @Column(name = "priority", length = 20)
    private String priority;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Column(name = "raised_by", length = 100)
    private String raisedBy;
    @Column(name = "status", length = 30)
    private String status;
    @Column(name = "created_at")
    private Instant createdAt;
    @PrePersist void pre() { createdAt = Instant.now(); }
}
