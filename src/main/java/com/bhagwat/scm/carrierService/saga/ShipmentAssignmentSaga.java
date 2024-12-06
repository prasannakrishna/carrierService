package com.bhagwat.scm.carrierService.saga;

import com.bhagwat.scm.carrierService.common.VolumeUOM;
import com.bhagwat.scm.carrierService.common.WeightUOM;
import com.bhagwat.scm.carrierService.dto.*;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Saga
public class ShipmentAssignmentSaga {

    @Autowired
    private transient CommandGateway commandGateway;

    private String consignmentId;

    @StartSaga
    @SagaEventHandler(associationProperty = "shipmentId")
    public void handle(CheckAndAssignCarrierCommand command) {
        CarrierRequestObject request = command.getRequestObject();

        // Validate carrier and contract
        if (!validateContract(request)) {
            commandGateway.send(new CarrierAssignmentFailedEvent(command.getShipmentId(), "Invalid contract or pincodes."));
            SagaLifecycle.end();
            return;
        }

        // Validate vehicle availability
        String vehicleId = findAvailableVehicle(request);
        if (vehicleId == null) {
            commandGateway.send(new CarrierAssignmentFailedEvent(command.getShipmentId(), "No suitable vehicle available."));
            SagaLifecycle.end();
            return;
        }

        // Create consignment
        this.consignmentId = UUID.randomUUID().toString();
        commandGateway.send(new CreateConsignmentCommand(
                command.getShipmentId(),
                request.getOrderId(),
                request.getHeight(),
                request.getWidth(),
                request.getLength(),
                calculateVolume(request),
                request.getWeight(),
                VolumeUOM.valueOf("L"),
                WeightUOM.KILOGRAM,
                request.getPickUpDate(),
                request.getDeliveryByDate()
        ));
    }

    @SagaEventHandler(associationProperty = "shipmentId")
    public void on(ConsignmentCreatedEvent event) {
        commandGateway.send(new LinkConsignmentToVehicleCommand(
                event.getConsignmentId(),
                "vehicle123", // Retrieved earlier
                event.getShipmentId()
        ));
    }

    @SagaEventHandler(associationProperty = "shipmentId")
    public void on(VehicleConsignmentLinkedEvent event) {
        System.out.println("Consignment linked successfully to vehicle!");
        SagaLifecycle.end();
    }

    private boolean validateContract(CarrierRequestObject request) {
        // Implement contract validation logic
        return true;
    }

    private String findAvailableVehicle(CarrierRequestObject request) {
        // Implement vehicle lookup logic
        return "vehicle123";
    }

    private double calculateVolume(CarrierRequestObject request) {
        return request.getHeight() * request.getWidth() * request.getLength();
    }
}
