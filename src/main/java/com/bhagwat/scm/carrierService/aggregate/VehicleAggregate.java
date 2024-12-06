package com.bhagwat.scm.carrierService.aggregate;


import com.bhagwat.scm.carrierService.dto.LinkConsignmentToVehicleCommand;
import com.bhagwat.scm.carrierService.dto.VehicleConsignmentLinkedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class VehicleAggregate {

    @AggregateIdentifier
    private String vehicleId;

    @CommandHandler
    public void handle(LinkConsignmentToVehicleCommand command) {
        AggregateLifecycle.apply(new VehicleConsignmentLinkedEvent(
                command.getVehicleId(),
                command.getConsignmentId()
        ));
    }

    @EventSourcingHandler
    public void on(VehicleConsignmentLinkedEvent event) {
        System.out.println("Consignment linked to vehicle: " + event.getVehicleId());
    }
}

