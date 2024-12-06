package com.bhagwat.scm.carrierService.dto;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class AssignVehicleCommand {
    @TargetAggregateIdentifier
    private String vehicleId;
    private String shipmentId;

    // Constructor, Getters, and Setters
}

