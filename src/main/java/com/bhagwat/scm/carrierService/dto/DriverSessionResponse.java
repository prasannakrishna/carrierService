package com.bhagwat.scm.carrierService.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DriverSessionResponse {
    private String driverId;
    private String driverName;
    private String carrierId;
    private String carrierName;
    private String vehicleId;
    private String vehicleNumber;
    private String vehicleType;
    private String token;
    private String serverUrl;
}
