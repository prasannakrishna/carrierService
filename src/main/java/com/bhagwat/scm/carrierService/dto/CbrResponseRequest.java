package com.bhagwat.scm.carrierService.dto;
import com.bhagwat.scm.carrierService.enums.RateType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CbrResponseRequest {
    @NotBlank private String cbrId;
    @NotBlank private String carrierId;
    private String carrierName;
    private String vehicleId;
    private LocalDate offeredPickupDate;
    private LocalDate offeredDeliveryDate;
    private BigDecimal rateOffered;
    private String currency;
    private RateType rateType;
    private String notes;
}
