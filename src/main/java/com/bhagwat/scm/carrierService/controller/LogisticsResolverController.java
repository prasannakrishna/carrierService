package com.bhagwat.scm.carrierService.controller;

import com.bhagwat.scm.carrierService.entity.CarrierServiceArea;
import com.bhagwat.scm.carrierService.repository.CarrierServiceAreaRepository;
import com.bhagwat.scm.carrierService.service.LogisticsResolverService;
import com.bhagwat.scm.carrierService.service.LogisticsResolverService.ShipmentPlan;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/logistics")
@RequiredArgsConstructor
public class LogisticsResolverController {

    private final LogisticsResolverService resolverService;
    private final CarrierServiceAreaRepository serviceAreaRepo;

    /**
     * Resolve logistics route from supply to demand.
     *
     * GET /api/v1/logistics/resolve?supplyPincode=560043&demandPincode=600001
     */
    @GetMapping("/resolve")
    public ResponseEntity<ShipmentPlan> resolveRoute(
            @RequestParam String supplyPincode,
            @RequestParam String demandPincode) {
        String supplyCluster = supplyPincode.length() >= 3 ? supplyPincode.substring(0, 3) : supplyPincode;
        String demandCluster = demandPincode.length() >= 3 ? demandPincode.substring(0, 3) : demandPincode;
        return ResponseEntity.ok(resolverService.resolveRoute(supplyCluster, demandCluster, supplyPincode, demandPincode));
    }

    /**
     * Register a carrier's service area.
     *
     * POST /api/v1/logistics/service-areas
     */
    @PostMapping("/service-areas")
    public ResponseEntity<CarrierServiceArea> registerServiceArea(@RequestBody CarrierServiceArea area) {
        return ResponseEntity.ok(serviceAreaRepo.save(area));
    }

    /**
     * Get all service areas for a carrier.
     *
     * GET /api/v1/logistics/service-areas?carrierId=xxx
     */
    @GetMapping("/service-areas")
    public ResponseEntity<List<CarrierServiceArea>> getServiceAreas(@RequestParam String carrierId) {
        return ResponseEntity.ok(serviceAreaRepo.findByCarrierIdAndActiveTrue(carrierId));
    }

    /**
     * Find last mile providers for a cluster.
     *
     * GET /api/v1/logistics/last-mile?cluster=560
     */
    @GetMapping("/last-mile")
    public ResponseEntity<List<CarrierServiceArea>> findLastMile(@RequestParam String cluster) {
        return ResponseEntity.ok(serviceAreaRepo.findByClusterPrefixAndServiceTypeAndActiveTrue(
                cluster, CarrierServiceArea.ServiceType.LAST_MILE));
    }

    /**
     * Find first mile providers for a cluster.
     *
     * GET /api/v1/logistics/first-mile?cluster=560
     */
    @GetMapping("/first-mile")
    public ResponseEntity<List<CarrierServiceArea>> findFirstMile(@RequestParam String cluster) {
        return ResponseEntity.ok(serviceAreaRepo.findByClusterPrefixAndServiceTypeInAndActiveTrue(
                cluster, List.of(CarrierServiceArea.ServiceType.FIRST_MILE, CarrierServiceArea.ServiceType.FULL_SERVICE)));
    }

    /**
     * Check serviceability: can we deliver from supply pincode to demand pincode?
     *
     * GET /api/v1/logistics/serviceability?supplyPincode=560043&demandPincode=600001
     */
    @GetMapping("/serviceability")
    public ResponseEntity<Map<String, Object>> checkServiceability(
            @RequestParam String supplyPincode,
            @RequestParam String demandPincode) {
        String supplyCluster = supplyPincode.substring(0, 3);
        String demandCluster = demandPincode.substring(0, 3);
        ShipmentPlan plan = resolverService.resolveRoute(supplyCluster, demandCluster, supplyPincode, demandPincode);

        return ResponseEntity.ok(Map.of(
                "serviceable", !"NO_ROUTE".equals(plan.getPlanType()),
                "planType", plan.getPlanType(),
                "totalLegs", plan.getTotalLegs(),
                "estimatedDays", plan.getEstimatedDays() != null ? plan.getEstimatedDays() : 0,
                "supplyCluster", supplyCluster,
                "demandCluster", demandCluster
        ));
    }
}
