package com.bhagwat.scm.carrierService.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ShipmentPartyDto {
    private String partyId;
    private String partyName;
    private String partyType;
    private String orgId;
    private String contactPhone;
}
