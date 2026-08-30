package com.bhagwat.scm.carrierService.entity;

import com.bhagwat.scm.carrierService.enums.ConsignmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity @Table(name = "consignments")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Consignment {
    @Id @Column(name = "consignment_id", nullable = false, updatable = false)
    private String consignmentId;

    @Column(name = "transport_shipment_id", nullable = false, length = 100)
    private String transportShipmentId;

    @Column(name = "order_id", length = 100)
    private String orderId;

    @Column(name = "destination_type", nullable = false, length = 30)
    private String destinationType;

    @Column(name = "destination_id", nullable = false, length = 100)
    private String destinationId;

    @Column(name = "destination_name", length = 255)
    private String destinationName;

    @Column(name = "label_code", nullable = false, unique = true, length = 50)
    private String labelCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private ConsignmentStatus status = ConsignmentStatus.MANIFESTED;

    @Column(name = "total_packages")
    private Integer totalPackages;

    @Column(name = "total_weight_kg", precision = 12, scale = 3)
    private BigDecimal totalWeightKg;

    @OneToMany(mappedBy = "consignment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ConsignmentItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (consignmentId == null) consignmentId = UUID.randomUUID().toString();
        createdAt = updatedAt = LocalDateTime.now();
        if (status == null) status = ConsignmentStatus.MANIFESTED;
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
