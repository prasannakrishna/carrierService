package com.bhagwat.scm.carrierService.client;

import com.bhagwat.scm.core.rest.api.ApiClient;
import com.bhagwat.scm.core.rest.config.ServiceApiRegistry;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component @Slf4j @RequiredArgsConstructor
public class ContractManagerClient {

    private final ApiClient apiClient;
    private final ServiceApiRegistry registry;

    public ContractTermsSummary getSellerCarrierContract(String sellerId, String carrierId) {
        try {
            Map<String, String> params = Map.of(
                    "party1Id", sellerId, "party2Id", carrierId,
                    "type", "SELLER_LOGISTICS", "status", "ACTIVE");
            ResponseEntity<Map[]> resp = apiClient.invoke(
                    registry.getConfig("contract-search"), null, params, null, Map[].class);
            Map[] contracts = resp.getBody();
            if (contracts == null || contracts.length == 0) return null;

            Map<String, Object> contract = contracts[0];
            Map<String, Object> logistics = (Map<String, Object>) contract.get("logisticsTerms");
            if (logistics == null) return null;
            Map<String, Object> sla = (Map<String, Object>) logistics.get("sla");

            return ContractTermsSummary.builder()
                    .contractId((String) contract.get("id"))
                    .contractNumber((String) contract.get("contractNumber"))
                    .rateType((String) logistics.get("rateType"))
                    .ftlRate(toBd(logistics.get("ftlRate")))
                    .ratePerKg(toBd(logistics.get("ratePerKg")))
                    .ratePerShipment(toBd(logistics.get("ratePerShipment")))
                    .pickupSlaDays(sla != null ? toInt(sla.get("pickupSlaDays")) : null)
                    .deliverySlaDays(sla != null ? toInt(sla.get("deliverySlaDays")) : null)
                    .penaltyPerDayDelay(sla != null ? toBd(sla.get("penaltyPerDayDelay")) : null)
                    .insuranceCoverage((Boolean) logistics.get("insuranceCoverage"))
                    .allowPartnerNetwork((Boolean) logistics.get("allowPartnerNetwork"))
                    .leadTimeHours(toInt(logistics.get("leadTimeToArrangeTransportHours")))
                    .build();
        } catch (Exception e) {
            log.debug("Could not fetch contract for seller={} carrier={}: {}", sellerId, carrierId, e.getMessage());
            return null;
        }
    }

    private BigDecimal toBd(Object val) { return val == null ? null : new BigDecimal(val.toString()); }
    private Integer toInt(Object val) { return val == null ? null : ((Number) val).intValue(); }

    @Data @Builder
    public static class ContractTermsSummary {
        private String contractId, contractNumber, rateType;
        private BigDecimal ftlRate, ratePerKg, ratePerShipment, penaltyPerDayDelay;
        private Integer pickupSlaDays, deliverySlaDays, leadTimeHours;
        private Boolean insuranceCoverage, allowPartnerNetwork;
    }
}
