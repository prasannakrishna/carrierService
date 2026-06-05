package com.bhagwat.scm.carrierService.dto;
import com.bhagwat.scm.carrierService.enums.VehicleStatus;
import com.bhagwat.scm.carrierService.enums.VehicleType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CarrierVehicleResponse {
    private String vehicleId;
    private String carrierId;
    private String fleetId;
    private String vehicleNumber;
    private VehicleType vehicleType;
    private BigDecimal capacityKg;
    private BigDecimal volumeCapacityCbm;
    private VehicleStatus status;
    private String driverName;
    private String driverPhone;
    private String driverLicense;
    private LocalDateTime createdAt;
}
