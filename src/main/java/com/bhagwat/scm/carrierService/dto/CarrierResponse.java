package com.bhagwat.scm.carrierService.dto;
import com.bhagwat.scm.carrierService.enums.CarrierType;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CarrierResponse {
    private String carrierId;
    private String carrierName;
    private String scacCode;
    private CarrierType carrierType;
    private String contactEmail;
    private String contactPhone;
    private LocationAddressDto address;
    private String gstin;
    private String panNumber;
    private Boolean active;
    private LocalDateTime createdAt;
}
