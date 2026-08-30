package com.bhagwat.scm.carrierService.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DeliveryReportRequestDto {
    @NotBlank private String consignmentId;
    private Integer countReported;
    private String conditionNotes;
    private List<String> photoUris;
    @NotBlank private String idempotencyKey;
}
