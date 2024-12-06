package com.bhagwat.scm.carrierService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleConsignmentLinkedEvent {
    private String vehicleId;
    private String consignmentId;

    // Constructor, Getters, and Setters
}

