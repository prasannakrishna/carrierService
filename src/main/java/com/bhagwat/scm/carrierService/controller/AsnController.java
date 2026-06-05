package com.bhagwat.scm.carrierService.controller;

import com.bhagwat.scm.carrierService.dto.AsnResponse;
import com.bhagwat.scm.carrierService.enums.AsnStatus;
import com.bhagwat.scm.carrierService.service.AsnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/carrier/asn")
@RequiredArgsConstructor
@Tag(name = "Advanced Shipment Notice", description = "ASN management for warehouses and stores")
public class AsnController {

    private final AsnService asnService;

    @GetMapping("/{asnId}")
    @Operation(summary = "Get ASN by ID")
    public ResponseEntity<AsnResponse> getAsn(@PathVariable String asnId) {
        return ResponseEntity.ok(asnService.getAsn(asnId));
    }

    @GetMapping
    @Operation(summary = "List ASNs for a receiving party (optionally filtered by status)")
    public ResponseEntity<List<AsnResponse>> listAsns(
            @RequestParam String partyId,
            @RequestParam(required = false) AsnStatus status) {
        if (status != null) {
            return ResponseEntity.ok(asnService.listByPartyAndStatus(partyId, status));
        }
        return ResponseEntity.ok(asnService.listByParty(partyId));
    }

    @GetMapping("/rts/{rtsId}")
    @Operation(summary = "List ASNs for a specific RTS order")
    public ResponseEntity<List<AsnResponse>> listByRts(@PathVariable String rtsId) {
        return ResponseEntity.ok(asnService.listByRts(rtsId));
    }

    @PostMapping("/{asnId}/acknowledge")
    @Operation(summary = "Acknowledge receipt of ASN (SENT → ACKNOWLEDGED)")
    public ResponseEntity<AsnResponse> acknowledgeAsn(@PathVariable String asnId) {
        return ResponseEntity.ok(asnService.acknowledgeAsn(asnId));
    }
}
