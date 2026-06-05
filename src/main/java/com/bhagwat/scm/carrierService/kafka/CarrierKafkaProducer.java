package com.bhagwat.scm.carrierService.kafka;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor @Slf4j
public class CarrierKafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCbrBroadcast(String cbrId, Object payload) {
        send("transport.booking.request.broadcast", cbrId, payload);
    }
    public void publishRtsCreated(String rtsId, Object payload) {
        send("transport.rts.created", rtsId, payload);
    }
    public void publishAsnSent(String asnId, Object payload) {
        send("transport.asn.sent", asnId, payload);
    }
    public void publishMilestone(String tsId, Object payload) {
        send("transport.shipment.milestone", tsId, payload);
    }
    public void publishShipmentDelivered(String tsId, Object payload) {
        send("transport.shipment.delivered", tsId, payload);
    }
    public void publishShipmentCreated(String toId, String tsId) {
        send("transport.shipment.created", toId, java.util.Map.of("toId", toId, "tsId", tsId));
    }
    private void send(String topic, String key, Object payload) {
        try {
            kafkaTemplate.send(topic, key, payload);
            log.info("Published to {} key={}", topic, key);
        } catch (Exception e) {
            log.error("Failed to publish to {}: {}", topic, e.getMessage());
        }
    }
}
