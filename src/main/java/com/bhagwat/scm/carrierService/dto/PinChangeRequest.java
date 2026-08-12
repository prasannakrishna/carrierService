package com.bhagwat.scm.carrierService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PinChangeRequest {

    @NotBlank
    private String currentPin;

    @NotBlank
    @Pattern(regexp = "^\\d{4}$", message = "New PIN must be exactly 4 numeric digits")
    private String newPin;
}
