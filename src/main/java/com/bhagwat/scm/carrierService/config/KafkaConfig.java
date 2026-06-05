package com.bhagwat.scm.carrierService.config;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import java.util.Map;
@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.bootstrap-servers}") private String bootstrapServers;

    @Bean
    @Primary
    public KafkaTemplate<String, String> kafkaTemplate() {
        var props = Map.<String, Object>of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class
        );
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    @Bean
    public KafkaTemplate<String, Object> objectKafkaTemplate() {
        var props = Map.<String, Object>of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
        );
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }
    @Bean public NewTopic bookingBroadcastTopic()  { return TopicBuilder.name("transport.booking.request.broadcast").partitions(3).replicas(1).build(); }
    @Bean public NewTopic rtsCreatedTopic()         { return TopicBuilder.name("transport.rts.created").partitions(3).replicas(1).build(); }
    @Bean public NewTopic asnSentTopic()            { return TopicBuilder.name("transport.asn.sent").partitions(3).replicas(1).build(); }
    @Bean public NewTopic milestoneTopic()          { return TopicBuilder.name("transport.shipment.milestone").partitions(3).replicas(1).build(); }
    @Bean public NewTopic shipmentDeliveredTopic()  { return TopicBuilder.name("transport.shipment.delivered").partitions(3).replicas(1).build(); }
    @Bean public NewTopic planCreatedTopic()        { return TopicBuilder.name("transport.plan.created").partitions(3).replicas(1).build(); }
}
