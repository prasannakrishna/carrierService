package com.bhagwat.scm.carrierService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckAndAssignCarrierCommand {
    @TargetAggregateIdentifier
    private String shipmentId;
    private CarrierRequestObject requestObject;

    // Constructor, Getters, and Setters
}

