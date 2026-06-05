package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "logistics_contracts")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LogisticsContract {
    @Id @Column(name = "contract_id", length = 50)
    private String contractId;
    @Column(name = "party", length = 200)
    private String party;
    @Column(name = "party_type", length = 50)
    private String partyType;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @Column(name = "value", precision = 14, scale = 2)
    private BigDecimal value;
    @Column(name = "status", length = 30)
    private String status;
}
