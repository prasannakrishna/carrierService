package com.bhagwat.scm.carrierService.dto;
import com.bhagwat.scm.carrierService.enums.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransportShipmentResponse {
    private String tsId;
    private String tsNumber;
    private String rtsId;
    private String rtsNumber;
    private String transportPlanId;
    private String transportPlanNumber;
    private String carrierId;
    private String carrierName;
    private String vehicleId;
    private String vehicleNumber;
    private String driverName;
    private String driverPhone;
    private ShipmentType shipmentType;
    private ShipmentPartyDto shipper;
    private ShipmentPartyDto consignee;
    private LocationAddressDto originAddress;
    private LocationAddressDto destinationAddress;
    private LocalDateTime actualPickupDateTime;
    private LocalDateTime estimatedDeliveryDateTime;
    private LocalDateTime actualDeliveryDateTime;
    private BigDecimal totalWeightKg;
    private BigDecimal totalVolumeM3;
    private Integer totalPackages;
    private String currentLocation;
    private TransportShipmentStatus status;
    private String failureReason;
    private LocalDateTime createdAt;
    private List<MilestoneResponse> milestones;
}
