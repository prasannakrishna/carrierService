package com.bhagwat.scm.carrierService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsignmentCreatedEvent {
    private String consignmentId;
    private String shipmentId;
    private String orderId;

    private LocalDate pickUpByDate;
    private LocalDate deliverByDate;

    // Constructor, Getters, and Setters
}

