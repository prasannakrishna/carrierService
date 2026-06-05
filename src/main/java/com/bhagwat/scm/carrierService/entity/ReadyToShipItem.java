package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity @Table(name = "ready_to_ship_items")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReadyToShipItem {
    @Id @Column(name = "rts_item_id", nullable = false, updatable = false)
    private String rtsItemId;

    @Column(name = "rts_id", nullable = false, length = 100)
    private String rtsId;

    @Column(name = "order_number", length = 100)
    private String orderNumber;

    @Column(name = "order_line_id", length = 100)
    private String orderLineId;

    @Column(name = "product_id", length = 100)
    private String productId;

    @Column(name = "product_name", length = 255)
    private String productName;

    @Column(name = "sku_id", length = 100)
    private String skuId;

    @Column(name = "variant_name", length = 255)
    private String variantName;

    @Column(name = "ordered_quantity", precision = 12, scale = 3)
    private BigDecimal orderedQuantity;

    /** Alias used by builder — maps to orderedQuantity for RTS item creation */
    @Column(name = "quantity", precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "packed_quantity", precision = 12, scale = 3)
    private BigDecimal packedQuantity;

    @Column(name = "package_quantity")
    private Integer packageQuantity;

    @Column(name = "weight_kg_per_unit", precision = 10, scale = 4)
    private BigDecimal weightKgPerUnit;

    @Column(name = "volume_m3_per_unit", precision = 10, scale = 5)
    private BigDecimal volumeM3PerUnit;

    @Column(name = "is_hazardous")
    @Builder.Default
    private Boolean isHazardous = false;

    @PrePersist
    protected void onCreate() {
        if (rtsItemId == null) rtsItemId = UUID.randomUUID().toString();
        if (isHazardous == null) isHazardous = false;
    }
}
