package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LastMileCarrier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long carrierId;

    private String carrierName;

    @ElementCollection
    private List<String> operatingPincodes;

    @OneToOne
    private Address carrierAddress;

    private Long contractId;

    // Getters and Setters
}

