package com.bhagwat.scm.carrierService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOperatorRequest {

    @NotNull
    private UUID facilityOwnerOrgId;

    @NotBlank
    @Size(max = 128)
    private String operatorName;

    @NotBlank
    @Pattern(regexp = "^\\d{4}$", message = "PIN must be exactly 4 numeric digits")
    private String pin;

    private String phoneNumber;

    private String vehicleType;

    @NotNull
    private UUID createdBy;
}
