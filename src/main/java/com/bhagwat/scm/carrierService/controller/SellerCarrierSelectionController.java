package com.bhagwat.scm.carrierService.controller;

import com.bhagwat.scm.carrierService.client.ContractManagerClient;
import com.bhagwat.scm.carrierService.entity.*;
import com.bhagwat.scm.carrierService.enums.*;
import com.bhagwat.scm.carrierService.kafka.CarrierKafkaProducer;
import com.bhagwat.scm.carrierService.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * APIs for sellers to view carrier quotes and select a carrier for their shipment.
 * Flow: SO marked ready → CBR broadcast → carriers respond → seller picks → plan created
 */
@RestController
@RequestMapping("/api/v1/carrier/seller-selection")
@RequiredArgsConstructor
public class SellerCarrierSelectionController {

    private final ReadyToShipOrderRepository rtsRepo;
    private final CarrierBookingRequestRepository cbrRepo;
    private final CarrierBookingResponseRepository cbrRespRepo;
    private final CarrierKafkaProducer kafkaProducer;
    private final ContractManagerClient contractClient;

    /**
     * Get all RTS orders for a seller that are awaiting carrier selection.
     */
    @GetMapping("/pending/{sellerId}")
    public ResponseEntity<List<ReadyToShipOrder>> getPendingForSeller(@PathVariable String sellerId) {
        List<ReadyToShipOrder> all = rtsRepo.findAll().stream()
                .filter(r -> r.getShipper() != null && sellerId.equals(r.getShipper().getPartyId()))
                .filter(r -> r.getStatus() == RtsStatus.READY && r.getCarrierId() == null)
                .toList();
        return ResponseEntity.ok(all);
    }

    /**
     * Get carrier responses (quotes) for a specific RTS/CBR.
     * Enriched with contract terms (SLA, rates) from contractManager.
     * Seller sees: quoted rate vs contract rate, ETA vs SLA commitment, vehicle type.
     */
    @GetMapping("/responses/{rtsId}")
    public ResponseEntity<List<Map<String, Object>>> getCarrierResponses(@PathVariable String rtsId) {
        // Find CBR for this RTS
        List<CarrierBookingRequest> cbrs = cbrRepo.findAll().stream()
                .filter(c -> rtsId.equals(c.getCbrId()))
                .toList();
        if (cbrs.isEmpty()) return ResponseEntity.ok(List.of());

        // Get seller ID from RTS
        String sellerId = rtsRepo.findById(rtsId)
                .map(r -> r.getShipper() != null ? r.getShipper().getPartyId() : null)
                .orElse(null);

        List<CarrierBookingResponse> responses = cbrs.stream()
                .flatMap(c -> cbrRespRepo.findByCbrId(c.getCbrId()).stream())
                .toList();

        // Enrich each response with contract terms
        List<Map<String, Object>> enriched = responses.stream().map(resp -> {
            Map<String, Object> result = new HashMap<>();
            result.put("responseId", resp.getCbrRespId());
            result.put("carrierId", resp.getCarrierId());
            result.put("carrierName", resp.getCarrierName());
            result.put("quotedRate", resp.getRateOffered());
            result.put("estimatedDeliveryDate", resp.getOfferedDeliveryDate());
            result.put("vehicleType", resp.getVehicleId());
            result.put("slaHours", null);
            result.put("status", resp.getStatus());

            // Fetch contract terms for this seller-carrier pair
            if (sellerId != null) {
                ContractManagerClient.ContractTermsSummary contract =
                        contractClient.getSellerCarrierContract(sellerId, resp.getCarrierId());
                if (contract != null) {
                    result.put("contractId", contract.getContractId());
                    result.put("contractNumber", contract.getContractNumber());
                    result.put("contractRateType", contract.getRateType());
                    result.put("contractFtlRate", contract.getFtlRate());
                    result.put("contractRatePerKg", contract.getRatePerKg());
                    result.put("contractRatePerShipment", contract.getRatePerShipment());
                    result.put("contractPickupSlaDays", contract.getPickupSlaDays());
                    result.put("contractDeliverySlaDays", contract.getDeliverySlaDays());
                    result.put("contractPenaltyPerDay", contract.getPenaltyPerDayDelay());
                    result.put("hasInsurance", contract.getInsuranceCoverage());
                    result.put("allowPartnerNetwork", contract.getAllowPartnerNetwork());
                    result.put("leadTimeHours", contract.getLeadTimeHours());
                    result.put("hasContract", true);
                } else {
                    result.put("hasContract", false);
                }
            }
            return result;
        }).toList();

        return ResponseEntity.ok(enriched);
    }

    /**
     * Seller selects a carrier from the responses.
     * This triggers: RTS assigned → Kafka rts.created → transportPlanner creates plan → execution begins.
     */
    @PostMapping("/select")
    public ResponseEntity<Map<String, String>> selectCarrier(@RequestBody CarrierSelectionRequest req) {
        ReadyToShipOrder rts = rtsRepo.findById(req.getRtsId())
                .orElseThrow(() -> new IllegalArgumentException("RTS not found: " + req.getRtsId()));

        // Assign selected carrier to RTS
        rts.setCarrierId(req.getCarrierId());
        rts.setCarrierName(req.getCarrierName());
        rts.setCbrId(req.getCbrResponseId());
        rts.setStatus(RtsStatus.BOOKED);
        rtsRepo.save(rts);

        // Decline all other responses for this RTS
        List<CarrierBookingRequest> cbrs = cbrRepo.findAll().stream()
                .filter(c -> req.getRtsId().equals(c.getCbrId())).toList();
        for (CarrierBookingRequest cbr : cbrs) {
            cbrRespRepo.findByCbrId(cbr.getCbrId()).forEach(resp -> {
                if (!resp.getCarrierId().equals(req.getCarrierId())) {
                    resp.setStatus(com.bhagwat.scm.carrierService.enums.CbrRespStatus.DECLINED);
                    cbrRespRepo.save(resp);
                } else {
                    resp.setStatus(com.bhagwat.scm.carrierService.enums.CbrRespStatus.ACCEPTED);
                    cbrRespRepo.save(resp);
                }
            });
        }

        // NOW trigger the transport planning flow
        kafkaProducer.publishRtsCreated(rts.getRtsId(), Map.of(
                "rtsId", rts.getRtsId(),
                "rtsNumber", rts.getRtsNumber(),
                "trId", rts.getTrId() != null ? rts.getTrId() : "",
                "carrierId", req.getCarrierId(),
                "carrierName", req.getCarrierName() != null ? req.getCarrierName() : "",
                "sellerId", rts.getShipper() != null ? rts.getShipper().getPartyId() : "",
                "originCity", rts.getOriginAddress() != null ? rts.getOriginAddress().getCity() : "",
                "destinationCity", rts.getDestinationAddress() != null ? rts.getDestinationAddress().getCity() : ""
        ));

        return ResponseEntity.ok(Map.of(
                "rtsId", rts.getRtsId(),
                "carrierId", req.getCarrierId(),
                "status", "BOOKED",
                "message", "Carrier selected. Transport plan will be created automatically."
        ));
    }

    @Data
    public static class CarrierSelectionRequest {
        private String rtsId;
        private String carrierId;
        private String carrierName;
        private String cbrResponseId; // which quote was accepted
    }
}
