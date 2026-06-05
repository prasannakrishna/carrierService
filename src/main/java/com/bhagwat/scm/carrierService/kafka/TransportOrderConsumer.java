package com.bhagwat.scm.carrierService.kafka;

import com.bhagwat.scm.carrierService.service.TransportShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consumes TransportOrder events from transportPlanner.
 * When a TO is created, carrierService creates a TransportShipment for execution.
 */
@Component @RequiredArgsConstructor @Slf4j
public class TransportOrderConsumer {

    private final TransportShipmentService shipmentService;
    private final CarrierKafkaProducer kafkaProducer;

    @KafkaListener(topics = "transport.order.created", groupId = "carrier-service")
    public void onTransportOrderCreated(Map<String, Object> event) {
        try {
            String toId = (String) event.get("toId");
            String toNumber = (String) event.get("toNumber");
            String planId = (String) event.get("planId");
            String carrierId = (String) event.get("carrierId");
            String vehicleId = (String) event.get("vehicleId");
            String vehicleNumber = (String) event.get("vehicleNumber");
            String driverName = (String) event.get("driverName");

            if (toId == null || carrierId == null) {
                log.warn("Ignoring TO event with missing toId or carrierId");
                return;
            }

            // Create a TransportShipment for this TO
            String tsId = shipmentService.createFromTransportOrder(
                    toId, toNumber, planId, carrierId, vehicleId, vehicleNumber, driverName, event);

            // Publish back to transportPlanner so it can link the shipment
            kafkaProducer.publishShipmentCreated(toId, tsId);

            log.info("Created shipment {} for TransportOrder {}", tsId, toId);
        } catch (Exception e) {
            log.error("Error processing transport.order.created: {}", e.getMessage(), e);
        }
    }
}
