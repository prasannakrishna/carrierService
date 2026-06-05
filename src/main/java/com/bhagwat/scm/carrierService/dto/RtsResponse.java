package com.bhagwat.scm.carrierService.dto;
import com.bhagwat.scm.carrierService.enums.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RtsResponse {
    private String rtsId;
    private String rtsNumber;
    private String trId;
    private String cbrId;
    private String carrierId;
    private String carrierName;
    private ShipmentType shipmentType;
    private ShipmentPartyDto shipper;
    private ShipmentPartyDto consignee;
    private LocationAddressDto originAddress;
    private LocationAddressDto destinationAddress;
    private LocalDateTime cargoReadyDateTime;
    private LocalDateTime cargoCutoffDateTime;
    private LoadType loadType;
    private BigDecimal totalWeightKg;
    private BigDecimal totalVolumeM3;
    private Integer totalPackages;
    private String incoterm;
    private String freightPaymentCode;
    private RtsStatus status;
    private Boolean asnSent;
    private LocalDateTime asnSentAt;
    private LocalDateTime createdAt;
    private List<RtsItemDto> items;
}
