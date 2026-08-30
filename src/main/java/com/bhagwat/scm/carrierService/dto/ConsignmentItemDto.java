package com.bhagwat.scm.carrierService.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ConsignmentItemDto {
    private String skuId;
    private String productName;
    private Integer quantity;
    private Double weightKg;
}
