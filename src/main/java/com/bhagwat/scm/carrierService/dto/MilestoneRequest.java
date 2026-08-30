package com.bhagwat.scm.carrierService.dto;
import com.bhagwat.scm.carrierService.enums.MilestoneType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MilestoneRequest {
    @NotBlank private String tsId;
    @NotNull private MilestoneType milestoneType;
    @NotNull private LocalDateTime milestoneDateTime;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String notes;
    private String postedBy;
    private String idempotencyKey;
}
