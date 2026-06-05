package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name = "rate_cards")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RateCard {
    @Id @Column(name = "rate_card_id", length = 50)
    private String rateCardId;
    @Column(name = "route", length = 200)
    private String route;
    @Column(name = "vehicle_type", length = 50)
    private String vehicleType;
    @Column(name = "rate_per_km", precision = 10, scale = 2)
    private BigDecimal ratePerKm;
    @Column(name = "min_weight", length = 50)
    private String minWeight;
    @Column(name = "status", length = 30)
    private String status;
}
