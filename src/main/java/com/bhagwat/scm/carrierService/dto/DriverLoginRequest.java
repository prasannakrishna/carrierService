package com.bhagwat.scm.carrierService.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DriverLoginRequest {
    @NotBlank private String vehicleNumber;
    @NotBlank private String driverPin;
}
