package com.bhagwat.scm.carrierService.entity;

import com.bhagwat.scm.carrierService.common.DriverStatus;
import com.bhagwat.scm.carrierService.common.VehicleStatus;
import com.bhagwat.scm.carrierService.common.VehicleType;
import jakarta.persistence.*;

@Entity
public class FirstMileCarrierVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vehicleId;

    private VehicleType vehicleType; // SMALL, MEDIUM, LARGECARGO

    private Long carrierId;

    @Enumerated(EnumType.STRING)
    private VehicleStatus status;

    private String currentLocationPincode;
    private Double maxWeight;
    private Double widthDimension;
    private Double height;
    private Double length;
    private String uom;

    private String driverName;
    private String driverContact;

    @Enumerated(EnumType.STRING)
    private DriverStatus driverAssigned;

    // Getters and Setters
}

