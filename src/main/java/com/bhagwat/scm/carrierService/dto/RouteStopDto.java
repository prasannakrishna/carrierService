package com.bhagwat.scm.carrierService.dto;
import lombok.*;
import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RouteStopDto {
    private String stopId;
    private Integer sequence;
    private String destinationId;
    private String destinationName;
    private String destinationType;
    private String address;
    private Double latitude;
    private Double longitude;
    private List<String> consignmentIds;
    private String status;
}
