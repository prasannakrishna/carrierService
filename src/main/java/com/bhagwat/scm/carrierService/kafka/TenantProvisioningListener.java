package com.bhagwat.scm.carrierService.kafka;

import com.bhagwat.scm.multitenancy.TenantSchemaProvisioner;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantProvisioningListener {

    private final TenantSchemaProvisioner provisioner;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "org.events", groupId = "carrier-tenant-provisioner")
    @SuppressWarnings("unchecked")
    public void onOrgEvent(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String eventType = (String) event.get("eventType");
            String tenantId = (String) event.get("tenantId");

            if (!"ORG_CREATED".equals(eventType)) return;
            if (tenantId == null || tenantId.isBlank()) return;

            String sanitized = tenantId.replace('-', '_');
            log.info("Provisioning carrier schema for tenant '{}'", sanitized);
            provisioner.provision(sanitized);
        } catch (Exception e) {
            log.error("Failed to provision carrier tenant: {}", e.getMessage());
        }
    }
}
