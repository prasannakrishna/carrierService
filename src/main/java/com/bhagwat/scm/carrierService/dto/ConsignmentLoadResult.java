package com.bhagwat.scm.carrierService.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ConsignmentLoadResult {
    private String consignmentId;
    private String status;
    private String message;
}
