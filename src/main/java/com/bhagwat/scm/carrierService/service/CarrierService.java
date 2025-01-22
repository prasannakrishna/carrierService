package com.bhagwat.scm.carrierService.service;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Service;

@Service
public class CarrierService {
    @EventHandler
    public void on(CarrierBookingRequestEvent event) {
        System.out.println("Processing carrier booking for Shipment: " + event.shipmentId);
        // Logic to select carrier and confirm booking
        String carrierId = "CARRIER_" + event.shipmentId;
        apply(new CarrierConfirmedEvent(event.shipmentId, carrierId));
    }
}
