package com.bhagwat.scm.carrierService.dto;
import lombok.*;
import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DeliveryReportDto {
    private String reportId;
    private String consignmentId;
    private Integer countReported;
    private String conditionNotes;
    private List<String> photos;
    private String submittedBy;
    private String status;
    private String approvalDeadline;
    private String approvedAt;
    private String approvedBy;
}
