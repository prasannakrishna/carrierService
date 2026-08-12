package com.bhagwat.scm.carrierService.controller;

import com.bhagwat.scm.carrierService.dto.*;
import com.bhagwat.scm.carrierService.entity.FacilityOperatorAssignment;
import com.bhagwat.scm.carrierService.repository.FacilityOperatorAssignmentRepository;
import com.bhagwat.scm.carrierService.service.FacilityOperatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Internal API endpoints called by the Operator Orchestrator.
 * Not exposed to external clients (protected via mTLS/network policy).
 */
@RestController
@RequestMapping("/internal/v1/operators")
@RequiredArgsConstructor
public class InternalOperatorController {

    private final FacilityOperatorService operatorService;
    private final FacilityOperatorAssignmentRepository assignmentRepository;

    /**
     * Credential verification — delegates PIN check to service.
     * R1.4: accepts operator_id and raw PIN, returns verification result without exposing hash.
     */
    @PostMapping("/verify-credential")
    public ResponseEntity<CredentialVerificationResponse> verifyCredential(
            @Valid @RequestBody CredentialVerificationRequest request) {
        CredentialVerificationResponse response = operatorService.verifyCredential(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Active assignment lookup — used by Orchestrator during login flow.
     * R2.2: verify that an ACTIVE assignment exists for operator at facility for current time.
     */
    @GetMapping("/{operatorId}/assignment")
    public ResponseEntity<?> getActiveAssignment(
            @PathVariable UUID operatorId,
            @RequestParam UUID facilityId,
            @RequestParam Instant currentTime) {

        return assignmentRepository.findActiveAssignment(operatorId, facilityId, currentTime)
                .map(assignment -> ResponseEntity.ok(AssignmentResponse.from(assignment)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PIN change — called by Orchestrator during forced rotation or self-initiated change.
     * R10.4: validates against PIN history, hashes with bcrypt.
     */
    @PostMapping("/{operatorId}/pin-change")
    public ResponseEntity<?> changePin(
            @PathVariable UUID operatorId,
            @Valid @RequestBody PinChangeRequest request) {
        try {
            boolean success = operatorService.changePin(operatorId, request);
            if (!success) {
                return ResponseEntity.status(401).body(new ErrorResponse("INVALID_CREDENTIALS", "Current PIN is incorrect"));
            }
            return ResponseEntity.ok(new PinChangeResponse(true, Instant.now()));
        } catch (FacilityOperatorService.PinReuseRejectedException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("PIN_REUSE_REJECTED", e.getMessage()));
        }
    }

    // Response DTOs (inner records for simplicity)
    record AssignmentResponse(UUID assignmentId, UUID facilityId, Instant shiftStart, Instant shiftEnd, String status) {
        static AssignmentResponse from(FacilityOperatorAssignment a) {
            return new AssignmentResponse(a.getId(), a.getFacilityId(), a.getShiftStart(), a.getShiftEnd(), a.getStatus().name());
        }
    }

    record PinChangeResponse(boolean success, Instant pinLastChangedAt) {}

    record ErrorResponse(String errorCode, String message) {}
}
