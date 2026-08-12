package com.bhagwat.scm.carrierService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialVerificationResponse {

    private boolean credentialValid;
    private String operatorStatus;
    private String owningService;
    private UUID facilityOwnerOrgId;
    private Instant pinLastChangedAt;
}
