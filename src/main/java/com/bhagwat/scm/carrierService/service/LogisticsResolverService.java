package com.bhagwat.scm.carrierService.service;

import com.bhagwat.scm.carrierService.entity.CarrierServiceArea;
import com.bhagwat.scm.carrierService.entity.CarrierServiceArea.ServiceType;
import com.bhagwat.scm.carrierService.repository.CarrierServiceAreaRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Resolves logistics (carrier selection) for a given supply→demand route.
 *
 * RESOLUTION CHAIN:
 *   1. FULL_SERVICE — carrier that does end-to-end (pickup + delivery)
 *   2. LAST_MILE DIRECT — if supply and demand are in same cluster
 *   3. FIRST_MILE + LAST_MILE — if both clusters have providers but no line-haul needed
 *   4. FIRST_MILE + MID_MILE + LAST_MILE — full multi-leg routing
 *
 * Each resolution returns a ShipmentPlan with 1-3 legs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LogisticsResolverService {

    private final CarrierServiceAreaRepository serviceAreaRepo;

    /**
     * Resolve the best logistics route from supply cluster to demand cluster.
     *
     * @param supplyCluster  3-digit pincode prefix of inventory location
     * @param demandCluster  3-digit pincode prefix of customer delivery
     * @param supplyPincode  full pincode of supply location
     * @param demandPincode  full pincode of customer
     * @return ShipmentPlan with legs, or empty plan if no carriers found
     */
    @Transactional(readOnly = true)
    public ShipmentPlan resolveRoute(String supplyCluster, String demandCluster,
                                      String supplyPincode, String demandPincode) {

        log.info("Resolving logistics: supply={} ({}) → demand={} ({})",
                supplyCluster, supplyPincode, demandCluster, demandPincode);

        // ── Option 1: Full service carrier covering both clusters ──
        if (supplyCluster.equals(demandCluster)) {
            // Same cluster — check for full service or last mile
            List<CarrierServiceArea> fullService = serviceAreaRepo
                    .findFullServiceForCluster(demandCluster);
            if (!fullService.isEmpty()) {
                CarrierServiceArea carrier = selectBestCarrier(fullService);
                return ShipmentPlan.builder()
                        .planType("DIRECT")
                        .totalLegs(1)
                        .estimatedDays(carrier.getEstimatedDays())
                        .legs(List.of(ShipmentLeg.builder()
                                .legNumber(1)
                                .legType("DIRECT")
                                .carrierId(carrier.getCarrierId())
                                .carrierName(carrier.getCarrierName())
                                .serviceType(ServiceType.FULL_SERVICE.name())
                                .fromPincode(supplyPincode)
                                .toPincode(demandPincode)
                                .fromCluster(supplyCluster)
                                .toCluster(demandCluster)
                                .estimatedDays(carrier.getEstimatedDays())
                                .build()))
                        .build();
            }

            // Same cluster — last mile provider can handle it
            List<CarrierServiceArea> lastMile = serviceAreaRepo
                    .findByClusterPrefixAndServiceTypeAndActiveTrue(demandCluster, ServiceType.LAST_MILE);
            if (!lastMile.isEmpty()) {
                CarrierServiceArea carrier = selectBestCarrier(lastMile);
                return ShipmentPlan.builder()
                        .planType("SAME_CLUSTER_LAST_MILE")
                        .totalLegs(1)
                        .estimatedDays(carrier.getEstimatedDays())
                        .legs(List.of(ShipmentLeg.builder()
                                .legNumber(1)
                                .legType("LAST_MILE")
                                .carrierId(carrier.getCarrierId())
                                .carrierName(carrier.getCarrierName())
                                .serviceType(ServiceType.LAST_MILE.name())
                                .fromPincode(supplyPincode)
                                .toPincode(demandPincode)
                                .fromCluster(supplyCluster)
                                .toCluster(demandCluster)
                                .estimatedDays(carrier.getEstimatedDays())
                                .build()))
                        .build();
            }
        }

        // ── Option 2: Different clusters — try full service first ──
        List<CarrierServiceArea> fullService = serviceAreaRepo
                .findFullServiceForCluster(demandCluster);
        // Check if any full service carrier also covers supply cluster
        for (CarrierServiceArea fs : fullService) {
            List<CarrierServiceArea> supplyCoverage = serviceAreaRepo
                    .findByCarrierIdAndActiveTrue(fs.getCarrierId()).stream()
                    .filter(a -> supplyCluster.equals(a.getClusterPrefix()))
                    .toList();
            if (!supplyCoverage.isEmpty()) {
                return ShipmentPlan.builder()
                        .planType("FULL_SERVICE_CROSS_CLUSTER")
                        .totalLegs(1)
                        .estimatedDays(fs.getEstimatedDays())
                        .legs(List.of(ShipmentLeg.builder()
                                .legNumber(1).legType("FULL_SERVICE")
                                .carrierId(fs.getCarrierId())
                                .carrierName(fs.getCarrierName())
                                .serviceType(ServiceType.FULL_SERVICE.name())
                                .fromPincode(supplyPincode).toPincode(demandPincode)
                                .fromCluster(supplyCluster).toCluster(demandCluster)
                                .estimatedDays(fs.getEstimatedDays())
                                .build()))
                        .build();
            }
        }

        // ── Option 3: Multi-leg — First Mile + Mid Mile + Last Mile ──
        List<CarrierServiceArea> firstMile = serviceAreaRepo
                .findByClusterPrefixAndServiceTypeInAndActiveTrue(supplyCluster,
                        List.of(ServiceType.FIRST_MILE, ServiceType.FULL_SERVICE));

        List<CarrierServiceArea> lastMile = serviceAreaRepo
                .findByClusterPrefixAndServiceTypeAndActiveTrue(demandCluster, ServiceType.LAST_MILE);

        List<CarrierServiceArea> midMile = serviceAreaRepo
                .findByOriginClusterAndDestinationClusterAndServiceTypeAndActiveTrue(
                        supplyCluster, demandCluster, ServiceType.MID_MILE);

        if (!firstMile.isEmpty() && !lastMile.isEmpty()) {
            List<ShipmentLeg> legs = new ArrayList<>();
            int totalDays = 0;

            // Leg 1: First Mile
            CarrierServiceArea fm = selectBestCarrier(firstMile);
            legs.add(ShipmentLeg.builder()
                    .legNumber(1).legType("FIRST_MILE")
                    .carrierId(fm.getCarrierId()).carrierName(fm.getCarrierName())
                    .serviceType(ServiceType.FIRST_MILE.name())
                    .fromPincode(supplyPincode).toPincode(null) // to hub
                    .fromCluster(supplyCluster).toCluster(supplyCluster)
                    .estimatedDays(fm.getEstimatedDays())
                    .build());
            totalDays += (fm.getEstimatedDays() != null ? fm.getEstimatedDays() : 1);

            // Leg 2: Mid Mile (if different clusters and line-haul available)
            if (!supplyCluster.equals(demandCluster) && !midMile.isEmpty()) {
                CarrierServiceArea mm = selectBestCarrier(midMile);
                legs.add(ShipmentLeg.builder()
                        .legNumber(2).legType("MID_MILE")
                        .carrierId(mm.getCarrierId()).carrierName(mm.getCarrierName())
                        .serviceType(ServiceType.MID_MILE.name())
                        .fromPincode(null).toPincode(null) // hub to hub
                        .fromCluster(supplyCluster).toCluster(demandCluster)
                        .estimatedDays(mm.getEstimatedDays())
                        .build());
                totalDays += (mm.getEstimatedDays() != null ? mm.getEstimatedDays() : 2);
            }

            // Leg 3 (or 2): Last Mile
            CarrierServiceArea lm = selectBestCarrier(lastMile);
            legs.add(ShipmentLeg.builder()
                    .legNumber(legs.size() + 1).legType("LAST_MILE")
                    .carrierId(lm.getCarrierId()).carrierName(lm.getCarrierName())
                    .serviceType(ServiceType.LAST_MILE.name())
                    .fromPincode(null).toPincode(demandPincode)
                    .fromCluster(demandCluster).toCluster(demandCluster)
                    .estimatedDays(lm.getEstimatedDays())
                    .build());
            totalDays += (lm.getEstimatedDays() != null ? lm.getEstimatedDays() : 1);

            String planType = midMile.isEmpty() ? "FIRST_MILE_LAST_MILE" : "FULL_MULTI_LEG";

            return ShipmentPlan.builder()
                    .planType(planType)
                    .totalLegs(legs.size())
                    .estimatedDays(totalDays)
                    .legs(legs)
                    .build();
        }

        // ── No carriers found ──
        log.warn("No logistics route found: supply={} → demand={}", supplyCluster, demandCluster);
        return ShipmentPlan.builder()
                .planType("NO_ROUTE")
                .totalLegs(0)
                .estimatedDays(null)
                .legs(List.of())
                .message("No carriers found for route " + supplyCluster + " → " + demandCluster)
                .build();
    }

    /**
     * Verifies whether a SPECIFIC carrier (the one already assigned to a
     * transport plan leg) actually covers a given pincode for the leg's
     * role — as opposed to resolveRoute(), which always picks whichever
     * carrier is best regardless of who's already been assigned. If the
     * assigned carrier isn't actually capable, returns a substitute if one
     * is registered for that pincode/role.
     *
     * @param role "PICKUP" (checks FIRST_MILE/FULL_SERVICE) or "DELIVERY" (checks LAST_MILE/FULL_SERVICE)
     */
    @Transactional(readOnly = true)
    public CapabilityCheck verifyCapability(String carrierId, String pincode, String role) {
        String cluster = pincode != null && pincode.length() >= 3 ? pincode.substring(0, 3) : pincode;
        List<ServiceType> validTypes = "PICKUP".equalsIgnoreCase(role)
                ? List.of(ServiceType.FIRST_MILE, ServiceType.FULL_SERVICE)
                : List.of(ServiceType.LAST_MILE, ServiceType.FULL_SERVICE);

        boolean capable = serviceAreaRepo.existsByCarrierIdAndClusterPrefixAndServiceTypeInAndActiveTrue(
                carrierId, cluster, validTypes);

        if (capable) {
            return CapabilityCheck.builder().capable(true).build();
        }

        List<CarrierServiceArea> candidates = serviceAreaRepo
                .findByClusterPrefixAndServiceTypeInAndActiveTrue(cluster, validTypes);
        if (candidates.isEmpty()) {
            log.warn("Carrier {} not {}-capable for cluster {}, and no substitute registered", carrierId, role, cluster);
            return CapabilityCheck.builder().capable(false).build();
        }

        CarrierServiceArea substitute = selectBestCarrier(candidates);
        log.info("Carrier {} not {}-capable for cluster {} — substitute {} found",
                carrierId, role, cluster, substitute.getCarrierId());
        return CapabilityCheck.builder()
                .capable(false)
                .substituteCarrierId(substitute.getCarrierId())
                .substituteCarrierName(substitute.getCarrierName())
                .build();
    }

    /**
     * Select the best carrier from a list — cheapest rate, then fastest.
     */
    private CarrierServiceArea selectBestCarrier(List<CarrierServiceArea> carriers) {
        return carriers.stream()
                .sorted(Comparator
                        .comparing((CarrierServiceArea c) -> c.getRatePerKg() != null ? c.getRatePerKg() : java.math.BigDecimal.valueOf(999))
                        .thenComparing(c -> c.getEstimatedDays() != null ? c.getEstimatedDays() : 999))
                .findFirst()
                .orElse(carriers.get(0));
    }

    // ── DTOs ─────────────────────────────────────────────────────────────

    @Data
    @Builder
    public static class ShipmentPlan {
        private String planType;     // DIRECT, SAME_CLUSTER_LAST_MILE, FULL_MULTI_LEG, NO_ROUTE
        private int totalLegs;
        private Integer estimatedDays;
        private String message;
        private List<ShipmentLeg> legs;
    }

    @Data
    @Builder
    public static class CapabilityCheck {
        private boolean capable;
        private String substituteCarrierId;
        private String substituteCarrierName;
    }

    @Data
    @Builder
    public static class ShipmentLeg {
        private int legNumber;
        private String legType;      // FIRST_MILE, MID_MILE, LAST_MILE, DIRECT, FULL_SERVICE
        private String carrierId;
        private String carrierName;
        private String serviceType;
        private String fromPincode;
        private String toPincode;
        private String fromCluster;
        private String toCluster;
        private Integer estimatedDays;
    }
}
