package com.bhagwat.scm.carrierService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LinkConsignmentToVehicleCommand {
    @TargetAggregateIdentifier
    private String vehicleId;

    private String consignmentId;
    private String shipmentId;

    // Constructor, Getters, and Setters
}

