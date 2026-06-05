package com.bhagwat.scm.carrierService.dto;
import com.bhagwat.scm.carrierService.enums.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AsnResponse {
    private String asnId;
    private String asnNumber;
    private String rtsId;
    private String rtsNumber;
    private String sentToPartyId;
    private String sentToPartyType;
    private String sentToPartyName;
    private String carrierId;
    private String carrierName;
    private ShipmentType shipmentType;
    private LocationAddressDto originAddress;
    private LocationAddressDto destinationAddress;
    private LocalDate expectedArrivalDate;
    private String deliveryWindow;
    private BigDecimal totalWeightKg;
    private BigDecimal totalVolumeM3;
    private Integer totalPackages;
    private AsnStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime createdAt;
}
