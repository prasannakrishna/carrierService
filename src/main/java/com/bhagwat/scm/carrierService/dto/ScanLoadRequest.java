package com.bhagwat.scm.carrierService.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ScanLoadRequest {
    @NotBlank private String consignmentBarcode;
}
