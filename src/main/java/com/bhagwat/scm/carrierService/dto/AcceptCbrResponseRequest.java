package com.bhagwat.scm.carrierService.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AcceptCbrResponseRequest {
    @NotBlank private String cbrRespId;
    private String shippingOrderId;
    private String shippingOrderNumber;
}
