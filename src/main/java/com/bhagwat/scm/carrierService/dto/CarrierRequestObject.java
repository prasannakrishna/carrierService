package com.bhagwat.scm.carrierService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarrierRequestObject {
    private String shipmentId;
    private String orderId;
    private String sellerId;
    private String customerId;
    private String sourcePincode;
    private String destinationPincode;
    private LocalDate pickUpDate;
    private LocalDate deliveryByDate;
    private double height;
    private double width;
    private double length;
    private double weight;
    private boolean isFullTruckNeeded;

    // Getters and Setters
}

