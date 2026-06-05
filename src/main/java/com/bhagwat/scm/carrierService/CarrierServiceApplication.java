package com.bhagwat.scm.carrierService;

import com.bhagwat.scm.observability.annotation.EnableObservability;
import com.bhagwat.scm.kafka.annotation.EnableKafkaMessaging;
import com.bhagwat.scm.core.rest.annotation.EnableRestClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableObservability
@EnableKafkaMessaging
@EnableRestClient
public class CarrierServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarrierServiceApplication.class, args);
	}

}
