package com.bhagwat.scm.carrierService.dto;
import lombok.*;
import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RoutePlanDto {
    private List<RouteStopDto> stops;
    private Double totalDistanceKm;
    private Integer estimatedDurationMin;
}
