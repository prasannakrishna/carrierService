package com.bhagwat.scm.carrierService.dto;
import lombok.*;
import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ConsignmentDto {
    private String consignmentId;
    private String transportShipmentId;
    private String orderId;
    private String destinationType;
    private String destinationId;
    private String destinationName;
    private String labelCode;
    private String status;
    private List<ConsignmentItemDto> items;
    private Integer totalPackages;
    private Double totalWeightKg;
}
