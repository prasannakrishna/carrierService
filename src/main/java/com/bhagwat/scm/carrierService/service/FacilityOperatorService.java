package com.bhagwat.scm.carrierService.service;

import com.bhagwat.scm.carrierService.dto.CredentialVerificationRequest;
import com.bhagwat.scm.carrierService.dto.CredentialVerificationResponse;
import com.bhagwat.scm.carrierService.dto.CreateOperatorRequest;
import com.bhagwat.scm.carrierService.dto.PinChangeRequest;
import com.bhagwat.scm.carrierService.entity.FacilityOperator;
import com.bhagwat.scm.carrierService.entity.FacilityOperator.OperatorStatus;
import com.bhagwat.scm.carrierService.entity.PinHistory;
import com.bhagwat.scm.carrierService.repository.FacilityOperatorRepository;
import com.bhagwat.scm.carrierService.repository.PinHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityOperatorService {

    private final FacilityOperatorRepository operatorRepository;
    private final PinHistoryRepository pinHistoryRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    private static final String OWNING_SERVICE = "CARRIER";

    @Transactional
    public FacilityOperator createOperator(CreateOperatorRequest request) {
        // Validate PIN format
        validatePinFormat(request.getPin());

        // Check uniqueness
        if (operatorRepository.existsByOwningServiceAndFacilityOwnerOrgIdAndOperatorName(
                OWNING_SERVICE, request.getFacilityOwnerOrgId(), request.getOperatorName())) {
            throw new DuplicateOperatorException("Operator with this name already exists in the organization");
        }

        // Hash PIN
        String pinHash = passwordEncoder.encode(request.getPin());

        FacilityOperator operator = FacilityOperator.builder()
                .owningService(OWNING_SERVICE)
                .facilityOwnerOrgId(request.getFacilityOwnerOrgId())
                .operatorName(request.getOperatorName())
                .pinHash(pinHash)
                .pinLastChangedAt(Instant.now())
                .status(OperatorStatus.ACTIVE)
                .phoneNumber(request.getPhoneNumber())
                .vehicleType(request.getVehicleType())
                .createdBy(request.getCreatedBy())
                .build();

        operator = operatorRepository.save(operator);

        // Save initial PIN to history
        pinHistoryRepository.save(PinHistory.builder()
                .operatorId(operator.getId())
                .pinHash(pinHash)
                .changedAt(Instant.now())
                .build());

        return operator;
    }

    /**
     * Credential verification — called by Operator Orchestrator via internal endpoint.
     * Returns verification result without exposing PIN hash externally.
     */
    public CredentialVerificationResponse verifyCredential(CredentialVerificationRequest request) {
        var operatorOpt = operatorRepository.findById(request.getOperatorId());

        if (operatorOpt.isEmpty()) {
            // Return indistinguishable-from-invalid response to prevent enumeration
            return CredentialVerificationResponse.builder()
                    .credentialValid(false)
                    .operatorStatus(null)
                    .owningService(OWNING_SERVICE)
                    .facilityOwnerOrgId(null)
                    .pinLastChangedAt(null)
                    .build();
        }

        FacilityOperator operator = operatorOpt.get();

        // Check status — reject suspended/deactivated with specific error
        if (operator.getStatus() == OperatorStatus.SUSPENDED) {
            return CredentialVerificationResponse.builder()
                    .credentialValid(false)
                    .operatorStatus("OPERATOR_SUSPENDED")
                    .owningService(OWNING_SERVICE)
                    .facilityOwnerOrgId(operator.getFacilityOwnerOrgId())
                    .pinLastChangedAt(operator.getPinLastChangedAt())
                    .build();
        }

        if (operator.getStatus() == OperatorStatus.DEACTIVATED) {
            return CredentialVerificationResponse.builder()
                    .credentialValid(false)
                    .operatorStatus("OPERATOR_DEACTIVATED")
                    .owningService(OWNING_SERVICE)
                    .facilityOwnerOrgId(operator.getFacilityOwnerOrgId())
                    .pinLastChangedAt(operator.getPinLastChangedAt())
                    .build();
        }

        // Verify PIN
        boolean valid = passwordEncoder.matches(request.getPin(), operator.getPinHash());

        return CredentialVerificationResponse.builder()
                .credentialValid(valid)
                .operatorStatus(operator.getStatus().name())
                .owningService(OWNING_SERVICE)
                .facilityOwnerOrgId(operator.getFacilityOwnerOrgId())
                .pinLastChangedAt(operator.getPinLastChangedAt())
                .build();
    }

    /**
     * PIN change with history check — rejects reuse of last 3 PINs.
     */
    @Transactional
    public boolean changePin(UUID operatorId, PinChangeRequest request) {
        validatePinFormat(request.getNewPin());

        FacilityOperator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new OperatorNotFoundException(operatorId));

        // Verify current PIN
        if (!passwordEncoder.matches(request.getCurrentPin(), operator.getPinHash())) {
            return false;
        }

        // Check PIN history (last 3)
        List<PinHistory> history = pinHistoryRepository.findTop3ByOperatorIdOrderByChangedAtDesc(operatorId);
        for (PinHistory entry : history) {
            if (passwordEncoder.matches(request.getNewPin(), entry.getPinHash())) {
                throw new PinReuseRejectedException("PIN has been used recently");
            }
        }

        // Hash and persist new PIN
        String newPinHash = passwordEncoder.encode(request.getNewPin());
        operator.setPinHash(newPinHash);
        operator.setPinLastChangedAt(Instant.now());
        operator.setStatus(OperatorStatus.ACTIVE); // clear PIN_RESET_REQUIRED if set
        operatorRepository.save(operator);

        // Add to history
        pinHistoryRepository.save(PinHistory.builder()
                .operatorId(operatorId)
                .pinHash(newPinHash)
                .changedAt(Instant.now())
                .build());

        return true;
    }

    private void validatePinFormat(String pin) {
        if (pin == null || !pin.matches("^\\d{4}$")) {
            throw new InvalidPinFormatException("PIN must be exactly 4 numeric digits");
        }
    }

    // Exception classes
    public static class DuplicateOperatorException extends RuntimeException {
        public DuplicateOperatorException(String msg) { super(msg); }
    }

    public static class OperatorNotFoundException extends RuntimeException {
        public OperatorNotFoundException(UUID id) { super("Operator not found: " + id); }
    }

    public static class PinReuseRejectedException extends RuntimeException {
        public PinReuseRejectedException(String msg) { super(msg); }
    }

    public static class InvalidPinFormatException extends RuntimeException {
        public InvalidPinFormatException(String msg) { super(msg); }
    }
}
