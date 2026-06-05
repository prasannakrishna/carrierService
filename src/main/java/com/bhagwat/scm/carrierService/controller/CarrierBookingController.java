package com.bhagwat.scm.carrierService.controller;

import com.bhagwat.scm.carrierService.dto.*;
import com.bhagwat.scm.carrierService.service.CarrierBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/carrier/bookings")
@RequiredArgsConstructor
@Tag(name = "Carrier Booking", description = "Carrier Booking Request lifecycle")
public class CarrierBookingController {

    private final CarrierBookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a Carrier Booking Request (CBR)")
    public ResponseEntity<CbrResponse> createCbr(@Valid @RequestBody CbrRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createCbr(request));
    }

    @GetMapping
    @Operation(summary = "List CBRs (optionally filter by partyId and/or status)")
    public ResponseEntity<List<CbrResponse>> listCbrs(
            @RequestParam(required = false) String partyId,
            @RequestParam(required = false) com.bhagwat.scm.carrierService.enums.CbrStatus status) {
        return ResponseEntity.ok(bookingService.listCbrs(partyId, status));
    }

    @GetMapping("/{cbrId}")
    @Operation(summary = "Get CBR by ID")
    public ResponseEntity<CbrResponse> getCbr(@PathVariable String cbrId) {
        return ResponseEntity.ok(bookingService.getCbr(cbrId));
    }

    @PostMapping("/{cbrId}/broadcast")
    @Operation(summary = "Broadcast CBR to contracted carriers")
    public ResponseEntity<Void> broadcastCbr(@PathVariable String cbrId,
                                              @RequestBody CarrierIdsRequest request) {
        bookingService.broadcastCbr(cbrId, request.getCarrierIds());
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{cbrId}/responses")
    @Operation(summary = "List carrier responses for a CBR")
    public ResponseEntity<List<CbrResponseDto>> listResponses(@PathVariable String cbrId) {
        return ResponseEntity.ok(bookingService.listResponsesForCbr(cbrId));
    }

    @PostMapping("/carrier-response")
    @Operation(summary = "Carrier submits a response (accept with rate / decline)")
    public ResponseEntity<CbrResponseDto> submitCarrierResponse(@Valid @RequestBody CbrResponseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.submitCarrierResponse(request));
    }

    @PostMapping("/broadcasts/{broadcastId}/decline")
    @Operation(summary = "Carrier declines a broadcast")
    public ResponseEntity<Void> declineResponse(@PathVariable String broadcastId) {
        bookingService.declineResponse(broadcastId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cbrId}/accept-response")
    @Operation(summary = "Shipper accepts a carrier response — creates TransportRequest")
    public ResponseEntity<TransportRequestResponse> acceptResponse(@PathVariable String cbrId,
                                                                    @Valid @RequestBody AcceptCbrResponseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.acceptResponse(cbrId, request));
    }
}
