package com.bhagwat.scm.carrierService.dto;
import com.bhagwat.scm.carrierService.enums.VehicleType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.math.BigDecimal;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CarrierVehicleRequest {
    @NotBlank private String carrierId;
    @NotBlank private String vehicleNumber;
    private String fleetId;
    private VehicleType vehicleType;
    private BigDecimal capacityKg;
    private BigDecimal volumeCapacityCbm;
    private String driverName;
    private String driverPhone;
    private String driverLicense;
}
