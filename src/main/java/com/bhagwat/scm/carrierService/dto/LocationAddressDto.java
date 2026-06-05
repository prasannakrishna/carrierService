package com.bhagwat.scm.carrierService.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LocationAddressDto {
    private String locationId;
    private String street;
    private String city;
    private String state;
    private String pincode;
    private String country;
}
