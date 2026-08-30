package com.bhagwat.scm.carrierService.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DriverMilestoneResponse {
    private String milestoneId;
    private String tsId;
    private String milestoneType;
    private String notes;
    private String postedAt;
}
