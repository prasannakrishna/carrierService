package com.bhagwat.scm.carrierService.dto;

import com.bhagwat.scm.carrierService.common.VolumeUOM;
import com.bhagwat.scm.carrierService.common.WeightUOM;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateConsignmentCommand {

    @TargetAggregateIdentifier
    private final String consignmentId;
    private final String shipmentId;
    private final String orderId;
    private final double height;
    private final double width;
    private final double length;
    private final double volume;
    private final double weight;
    private final VolumeUOM volumeUOM;
    private final WeightUOM weightUOM;
    private final LocalDate pickUpDate;
    private final LocalDate deliveryByDate;

    // Constructor to take the parameters as provided
    public CreateConsignmentCommand(
            String shipmentId,
            String orderId,
            double height,
            double width,
            double length,
            double volume,
            double weight,
            VolumeUOM volumeUOM,
            WeightUOM weightUOM,
            LocalDate pickUpDate,
            LocalDate deliveryByDate) {

        // Use unique ConsignmentId generation logic (e.g., UUID or DB auto-generated value)
        this.consignmentId = UUID.randomUUID().toString();  // Assuming consignmentId is generated dynamically
        this.shipmentId = shipmentId;
        this.orderId = orderId;
        this.height = height;
        this.width = width;
        this.length = length;
        this.volume = volume;
        this.weight = weight;
        this.volumeUOM = volumeUOM;
        this.weightUOM = weightUOM;
        this.pickUpDate = pickUpDate;
        this.deliveryByDate = deliveryByDate;
    }

    // Getters for all fields
    public String getConsignmentId() {
        return consignmentId;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public double getLength() {
        return length;
    }

    public double getVolume() {
        return volume;
    }

    public double getWeight() {
        return weight;
    }

    public VolumeUOM getVolumeUOM() {
        return volumeUOM;
    }

    public WeightUOM getWeightUOM() {
        return weightUOM;
    }

    public LocalDate getPickUpDate() {
        return pickUpDate;
    }

    public LocalDate getDeliveryByDate() {
        return deliveryByDate;
    }
}
