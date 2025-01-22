package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long carrierId;

    @ElementCollection
    private List<String> operatingPincodes;


    private boolean status; // ACTIVE, INACTIVE

    private Double ratePerConsignment;
    private Double ratePerDistance;
    private Double fullTruckRate;
    private Double partialTruckRate;
    private Integer maxDaysForLogistics;


    // Getters and Setters
}

