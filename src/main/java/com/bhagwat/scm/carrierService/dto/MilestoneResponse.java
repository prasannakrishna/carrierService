package com.bhagwat.scm.carrierService.dto;
import com.bhagwat.scm.carrierService.enums.MilestoneType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MilestoneResponse {
    private String milestoneId;
    private String tsId;
    private MilestoneType milestoneType;
    private LocalDateTime milestoneDateTime;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String notes;
    private String postedBy;
    private LocalDateTime createdAt;
}
