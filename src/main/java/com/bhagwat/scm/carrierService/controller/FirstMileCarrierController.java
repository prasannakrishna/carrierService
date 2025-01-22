package com.bhagwat.scm.carrierService.controller;

import com.bhagwat.scm.carrierService.dto.AssignCarrierRequest;
import com.bhagwat.scm.carrierService.dto.CheckAndAssignCarrierCommand;
import com.bhagwat.scm.carrierService.entity.FirstMileCarrier;
import com.bhagwat.scm.carrierService.service.FirstMileCarrierService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/first-mile-carriers")
public class FirstMileCarrierController {

    @Autowired
    private FirstMileCarrierService firstMileCarrierService;

    private CommandGateway commandGateway;
    @Autowired
    public void ShipmentController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @PostMapping
    public ResponseEntity<FirstMileCarrier> createCarrier(@RequestBody FirstMileCarrier carrier) {
        return ResponseEntity.ok(firstMileCarrierService.createCarrier(carrier));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FirstMileCarrier> updateCarrier(@PathVariable Long id, @RequestBody FirstMileCarrier carrier) {
        return ResponseEntity.ok(firstMileCarrierService.updateCarrier(id, carrier));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FirstMileCarrier> getCarrierById(@PathVariable Long id) {
        return ResponseEntity.ok(firstMileCarrierService.getCarrierById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCarrier(@PathVariable Long id) {
        firstMileCarrierService.deleteCarrier(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assignCarrier")
    public ResponseEntity<String> assignCarrier(@RequestBody AssignCarrierRequest request) {
        // Creating the command object
        CheckAndAssignCarrierCommand command = new CheckAndAssignCarrierCommand(
                request.getShipmentId(),
                request.getCarrierId(),
                request.getStatus()
        );

        // Sending the command to the CommandGateway, which will route it to the saga
        commandGateway.send(command)
                .exceptionally(ex -> {
                    // Handle failure
                    return null;
                });

        return ResponseEntity.ok("Carrier assignment initiated");
    }
}

