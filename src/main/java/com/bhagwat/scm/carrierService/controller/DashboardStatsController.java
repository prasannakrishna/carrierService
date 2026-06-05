package com.bhagwat.scm.carrierService.controller;

import com.bhagwat.scm.carrierService.repository.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carrier/dashboard")
@RequiredArgsConstructor
public class DashboardStatsController {

    private final TransportShipmentRepository shipmentRepo;
    private final DriverRepository driverRepo;
    private final FleetRepository fleetRepo;
    private final CarrierVehicleRepository vehicleRepo;
    private final TransportExceptionRepository exceptionRepo;
    private final ReadyToShipOrderRepository rtsRepo;

    @Data @Builder
    public static class DashboardStats {
        private long totalShipments;
        private long activeDrivers;
        private long totalFleets;
        private long totalVehicles;
        private long openExceptions;
        private long pendingRts;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getStats() {
        return ResponseEntity.ok(DashboardStats.builder()
                .totalShipments(shipmentRepo.count())
                .activeDrivers(driverRepo.findByStatus("Active").size())
                .totalFleets(fleetRepo.count())
                .totalVehicles(vehicleRepo.count())
                .openExceptions(exceptionRepo.findByStatus("Open").size())
                .pendingRts(rtsRepo.count())
                .build());
    }
}
