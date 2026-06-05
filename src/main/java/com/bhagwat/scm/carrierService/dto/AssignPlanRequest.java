package com.bhagwat.scm.carrierService.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AssignPlanRequest {
    private String transportPlanId;
    private String transportPlanNumber;
}
