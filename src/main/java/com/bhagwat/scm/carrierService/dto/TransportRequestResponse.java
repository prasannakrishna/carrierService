package com.bhagwat.scm.carrierService.dto;
import com.bhagwat.scm.carrierService.enums.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransportRequestResponse {
    private String trId;
    private String trNumber;
    private String cbrId;
    private String cbrRespId;
    private String carrierId;
    private String carrierName;
    private String shippingOrderId;
    private String shippingOrderNumber;
    private String requestedByPartyId;
    private String requestedByPartyType;
    private String requestedByPartyName;
    private ShipmentType shipmentType;
    private LocationAddressDto originAddress;
    private LocationAddressDto destinationAddress;
    private LocalDate cargoReadyDate;
    private LocalDate requestedPickupDate;
    private LocalDate requestedDeliveryDate;
    private LoadType loadType;
    private BigDecimal totalWeightKg;
    private BigDecimal totalVolumeM3;
    private Integer totalPackages;
    private BigDecimal agreedRate;
    private String currency;
    private TransportRequestStatus status;
    private LocalDateTime createdAt;
    private List<TransportRequestItemDto> items;
}
