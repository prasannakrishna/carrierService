package com.bhagwat.scm.carrierService.entity;

import com.bhagwat.scm.carrierService.common.VolumeUOM;
import com.bhagwat.scm.carrierService.common.WeightUOM;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Consignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String consignmentId;
    private String shipmentId;
    private String orderId;
    private double height;
    private double width;
    private double length;
    private double volume;
    private double weight;

    @Enumerated(EnumType.STRING)
    private VolumeUOM volumeUOM;

    @Enumerated(EnumType.STRING)
    private WeightUOM weightUOM;

    private LocalDate pickUpByDate;
    private LocalDate deliverByDate;

    // Getters and Setters
}
