package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "booking_pincodes")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BookingPincode {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "pincode", nullable = false, length = 10)
    private String pincode;
    @Column(name = "city", length = 100)
    private String city;
    @Column(name = "state", length = 100)
    private String state;
    @Column(name = "carrier", length = 100)
    private String carrier;
    @Column(name = "cutoff_time", length = 20)
    private String cutoffTime;
}
