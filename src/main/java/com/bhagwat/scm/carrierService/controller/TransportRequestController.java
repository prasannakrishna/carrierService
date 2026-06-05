package com.bhagwat.scm.carrierService.controller;

import com.bhagwat.scm.carrierService.dto.*;
import com.bhagwat.scm.carrierService.service.TransportRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/carrier/transport-requests")
@RequiredArgsConstructor
@Tag(name = "Transport Request", description = "Transport Request and RTS creation")
public class TransportRequestController {

    private final TransportRequestService transportRequestService;

    @GetMapping("/{trId}")
    @Operation(summary = "Get Transport Request by ID")
    public ResponseEntity<TransportRequestResponse> getTransportRequest(@PathVariable String trId) {
        return ResponseEntity.ok(transportRequestService.getTransportRequest(trId));
    }

    @PostMapping("/{trId}/items")
    @Operation(summary = "Add shipment items to a Transport Request")
    public ResponseEntity<TransportRequestResponse> addItems(@PathVariable String trId,
                                                              @Valid @RequestBody List<TransportRequestItemDto> items) {
        return ResponseEntity.ok(transportRequestService.addItems(trId, items));
    }

    @PostMapping("/{trId}/create-rts")
    @Operation(summary = "Convert Transport Request to ReadyToShip Order, send ASN, create TransportShipment")
    public ResponseEntity<RtsResponse> createRts(@PathVariable String trId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transportRequestService.createRts(trId));
    }
}
