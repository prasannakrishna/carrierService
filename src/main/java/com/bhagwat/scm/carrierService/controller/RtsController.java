package com.bhagwat.scm.carrierService.controller;

import com.bhagwat.scm.carrierService.dto.RtsResponse;
import com.bhagwat.scm.carrierService.enums.RtsStatus;
import com.bhagwat.scm.carrierService.service.RtsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/carrier/rts")
@RequiredArgsConstructor
@Tag(name = "Ready To Ship", description = "ReadyToShip Order management")
public class RtsController {

    private final RtsService rtsService;

    @GetMapping("/{rtsId}")
    @Operation(summary = "Get RTS by ID (includes items)")
    public ResponseEntity<RtsResponse> getRts(@PathVariable String rtsId) {
        return ResponseEntity.ok(rtsService.getRts(rtsId));
    }

    @GetMapping
    @Operation(summary = "List RTS orders by carrier or status")
    public ResponseEntity<List<RtsResponse>> listRts(
            @RequestParam(required = false) String carrierId,
            @RequestParam(required = false) RtsStatus status) {
        if (carrierId != null) {
            return ResponseEntity.ok(rtsService.listByCarrier(carrierId));
        } else if (status != null) {
            return ResponseEntity.ok(rtsService.listByStatus(status));
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/{rtsId}/approve")
    @Operation(summary = "Approve an RTS order (DRAFT → APPROVED)")
    public ResponseEntity<RtsResponse> approveRts(@PathVariable String rtsId) {
        return ResponseEntity.ok(rtsService.approveRts(rtsId));
    }
}
