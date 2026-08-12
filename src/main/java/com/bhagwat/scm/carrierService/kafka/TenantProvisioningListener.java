package com.bhagwat.scm.carrierService.kafka;

import com.bhagwat.scm.multitenancy.TenantSchemaProvisioner;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Listens for ORG_CREATED events and provisions the carrier tenant schema
 * ONLY when the org's partyType is CARRIER.
 *
 * The provisioner runs Flyway migrations from classpath:db/migration/tenants
 * which contain the same DDL as the default schema (drivers, fleets, vehicles, etc.).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantProvisioningListener {

    private static final Set<String> ACCEPTED_PARTY_TYPES = Set.of("CARRIER");

    private final TenantSchemaProvisioner provisioner;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "org.events", groupId = "carrier-tenant-provisioner")
    @SuppressWarnings("unchecked")
    public void onOrgEvent(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String eventType = (String) event.get("eventType");
            String tenantId = (String) event.get("tenantId");
            String divisionType = (String) event.get("divisionType");

            if (!"ORG_CREATED".equals(eventType)) return;
            if (tenantId == null || tenantId.isBlank()) {
                log.warn("ORG_CREATED event missing tenantId, skipping");
                return;
            }

            // Only provision for CARRIER orgs
            if (divisionType == null || !ACCEPTED_PARTY_TYPES.contains(divisionType.toUpperCase())) {
                log.info("Skipping tenant provisioning — partyType '{}' not handled by carrierService", divisionType);
                return;
            }

            String sanitized = tenantId.replace('-', '_').toLowerCase();
            log.info("Provisioning carrier schema for tenant '{}' (partyType={})", sanitized, divisionType);
            provisioner.provision(sanitized);

        } catch (Exception e) {
            log.error("Failed to process org event for tenant provisioning: {}", e.getMessage(), e);
        }
    }
}
