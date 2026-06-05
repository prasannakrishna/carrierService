package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationAddress {
    @Column(length = 100)
    private String locationId;
    @Column(length = 255)
    private String street;
    @Column(length = 100)
    private String city;
    @Column(length = 100)
    private String state;
    @Column(length = 20)
    private String pincode;
    @Column(length = 100)
    private String country;
}
