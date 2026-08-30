package com.bhagwat.scm.carrierService.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DriverShipmentDto {
    private String tsId;
    private String tsNumber;
    private String carrierId;
    private String vehicleId;
    private String vehicleNumber;
    private String driverId;
    private String status;
    private Integer totalConsignments;
    private Integer loadedConsignments;
    private String plannedPickup;
    private String plannedDelivery;
    private String lastMilestoneAt;
    private RoutePlanDto routePlan;
}
