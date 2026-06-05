package com.bhagwat.scm.carrierService.kafka;
import com.bhagwat.scm.carrierService.dto.AssignPlanRequest;
import com.bhagwat.scm.carrierService.service.TransportShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.Map;
@Component @RequiredArgsConstructor @Slf4j
public class TransportPlanConsumer {
    private final TransportShipmentService shipmentService;

    @KafkaListener(topics = "transport.plan.created", groupId = "carrier-service")
    public void onPlanCreated(Map<String, Object> event) {
        try {
            String rtsId = (String) event.get("rtsId");
            String planId = (String) event.get("planId");
            String planNumber = (String) event.get("planNumber");
            if (rtsId == null || planId == null) return;
            shipmentService.assignTransportPlanByRtsId(rtsId, planId, planNumber);
            log.info("Assigned plan {} to shipments for rtsId={}", planId, rtsId);
        } catch (Exception e) {
            log.error("Error processing plan.created event: {}", e.getMessage());
        }
    }
}
