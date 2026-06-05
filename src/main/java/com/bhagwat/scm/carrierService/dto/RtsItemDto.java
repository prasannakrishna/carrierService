package com.bhagwat.scm.carrierService.dto;
import lombok.*;
import java.math.BigDecimal;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RtsItemDto {
    private String rtsItemId;
    private String rtsId;
    private String orderNumber;
    private String orderLineId;
    private String productId;
    private String productName;
    private String skuId;
    private String variantName;
    private BigDecimal orderedQuantity;
    private BigDecimal packedQuantity;
    private Integer packageQuantity;
    private BigDecimal weightKgPerUnit;
    private BigDecimal volumeM3PerUnit;
    private Boolean isHazardous;
}
