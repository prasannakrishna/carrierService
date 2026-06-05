package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "operating_pincodes")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OperatingPincode {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pincode", nullable = false, length = 10)
    private String pincode;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "zone", length = 50)
    private String zone;

    /** PICKUP, DROP, BOTH */
    @Column(name = "service_type", length = 20)
    @Builder.Default
    private String serviceType = "BOTH";

    @Column(name = "status", length = 30)
    @Builder.Default
    private String status = "Active";
}
