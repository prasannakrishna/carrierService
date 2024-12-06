package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Setter
@Getter
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String line1;
    private String line2;
    private String line3;
    private String post;
    private String city;
    private String district;
    private String state;
    private String country;
    private String pincode;

    private String contactNumber;
    private String email;

    // Getters and Setters

}

