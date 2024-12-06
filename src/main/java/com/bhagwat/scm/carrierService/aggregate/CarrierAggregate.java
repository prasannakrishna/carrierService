package com.bhagwat.scm.carrierService.aggregate;

import com.bhagwat.scm.carrierService.dto.ConsignmentCreatedEvent;
import com.bhagwat.scm.carrierService.dto.CreateConsignmentCommand;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.time.LocalDate;

@Aggregate
public class CarrierAggregate {

    @AggregateIdentifier
    private String consignmentId;

    private String shipmentId;
    private String orderId;

    private LocalDate pickUpByDate;
    private LocalDate deliverByDate;
    // Default constructor for Axon
    public CarrierAggregate() {
    }

    @CommandHandler
    public CarrierAggregate(CreateConsignmentCommand command) {
        // Validate the incoming command
        if (command.getHeight() <= 0 || command.getWidth() <= 0 || command.getLength() <= 0) {
            throw new IllegalArgumentException("Dimensions must be positive.");
        }

        if (command.getWeight() <= 0) {
            throw new IllegalArgumentException("Weight must be positive.");
        }

        // Calculate the volume (if required)
        double calculatedVolume = command.getHeight() * command.getWidth() * command.getLength();

        // Apply the event
        AggregateLifecycle.apply(new ConsignmentCreatedEvent(command.getConsignmentId(),command.getShipmentId(),command.getOrderId(),
                command.getPickUpDate(), command.getDeliveryByDate()));
    }

    @EventSourcingHandler
    public void on(ConsignmentCreatedEvent event) {
        this.consignmentId = event.getConsignmentId();
       this.shipmentId = event.getShipmentId();
       this.orderId = event.getOrderId();
       this.pickUpByDate = event.getPickUpByDate();
       this.deliverByDate = event.getDeliverByDate();
    }
}
