package com.bhagwat.scm.carrierService.dto;
import com.bhagwat.scm.carrierService.enums.CarrierType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CarrierRequest {
    @NotBlank private String carrierName;
    private String scacCode;
    private CarrierType carrierType;
    private String contactEmail;
    private String contactPhone;
    private LocationAddressDto address;
    private String gstin;
    private String panNumber;
}
