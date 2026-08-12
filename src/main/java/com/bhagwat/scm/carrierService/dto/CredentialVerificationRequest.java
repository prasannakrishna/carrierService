package com.bhagwat.scm.carrierService.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialVerificationRequest {

    @NotNull
    private UUID operatorId;

    @NotNull
    private String pin;
}
