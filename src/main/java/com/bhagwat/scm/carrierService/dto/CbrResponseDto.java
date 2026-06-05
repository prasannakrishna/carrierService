package com.bhagwat.scm.carrierService.dto;
import com.bhagwat.scm.carrierService.enums.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CbrResponseDto {
    private String cbrRespId;
    private String cbrRespNumber;
    private String cbrId;
    private String carrierId;
    private String carrierName;
    private String vehicleId;
    private LocalDate offeredPickupDate;
    private LocalDate offeredDeliveryDate;
    private BigDecimal rateOffered;
    private String currency;
    private RateType rateType;
    private String notes;
    private CbrRespStatus status;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
}
