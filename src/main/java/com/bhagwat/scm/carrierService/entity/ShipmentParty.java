package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentParty {
    @Column(length = 100)
    private String partyId;
    @Column(length = 255)
    private String partyName;
    @Column(length = 50)
    private String partyType;
    @Column(length = 100)
    private String orgId;
    @Column(length = 30)
    private String contactPhone;
}
