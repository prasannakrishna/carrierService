package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class VehicleConsignmentLinking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long lastMileCarrierVehicleId;
    private String consignmentId;

    // Getters and Setters
}

