package com.bhagwat.scm.carrierService.dto;
import com.bhagwat.scm.carrierService.enums.VehicleStatus;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class VehicleStatusRequest {
    private VehicleStatus status;
}
