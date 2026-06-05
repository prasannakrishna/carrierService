package com.bhagwat.scm.carrierService.dto;
import com.bhagwat.scm.carrierService.enums.LoadType;
import com.bhagwat.scm.carrierService.enums.ShipmentType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CbrRequest {
    private String requestedByPartyId;
    private String requestedByPartyType;
    private String requestedByPartyName;
    @NotNull private ShipmentType shipmentType;
    private LocationAddressDto originAddress;
    private LocationAddressDto destinationAddress;
    private LocalDate cargoReadyDate;
    private LocalDate requestedPickupDate;
    private LocalDate requestedDeliveryDate;
    private LoadType loadType;
    private BigDecimal totalWeightKg;
    private BigDecimal totalVolumeM3;
    private Integer totalPackages;
    private String specialInstructions;
    private String contractId;
}
