package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity @Table(name = "consignment_items")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ConsignmentItem {
    @Id @GeneratedValue @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consignment_id", nullable = false)
    private Consignment consignment;

    @Column(name = "sku_id", nullable = false, length = 100)
    private String skuId;

    @Column(name = "product_name", length = 255)
    private String productName;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "weight_kg", precision = 12, scale = 3)
    private BigDecimal weightKg;
}
