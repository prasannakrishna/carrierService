package com.bhagwat.scm.carrierService.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DriverMilestoneRequest {
    @NotBlank private String milestoneType;
    private String notes;
    private Double latitude;
    private Double longitude;
    @NotBlank private String idempotencyKey;
}
