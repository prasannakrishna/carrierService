package com.bhagwat.scm.carrierService.entity;

import com.bhagwat.scm.carrierService.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "transport_requests")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransportRequest {
    @Id @Column(name = "tr_id", nullable = false, updatable = false)
    private String trId;

    @Column(name = "tr_number", unique = true, nullable = false, length = 30)
    private String trNumber;

    @Column(name = "cbr_id", length = 100)
    private String cbrId;

    @Column(name = "cbr_resp_id", length = 100)
    private String cbrRespId;

    @Column(name = "carrier_id", length = 100)
    private String carrierId;

    @Column(name = "carrier_name", length = 255)
    private String carrierName;

    @Column(name = "shipping_order_id", length = 100)
    private String shippingOrderId;

    @Column(name = "shipping_order_number", length = 100)
    private String shippingOrderNumber;

    @Column(name = "requested_by_party_id", length = 100)
    private String requestedByPartyId;

    @Column(name = "requested_by_party_type", length = 30)
    private String requestedByPartyType;

    @Column(name = "requested_by_party_name", length = 255)
    private String requestedByPartyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipment_type", length = 30)
    private ShipmentType shipmentType;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "locationId", column = @Column(name = "orig_location_id")),
        @AttributeOverride(name = "street",     column = @Column(name = "orig_street")),
        @AttributeOverride(name = "city",        column = @Column(name = "orig_city")),
        @AttributeOverride(name = "state",       column = @Column(name = "orig_state")),
        @AttributeOverride(name = "pincode",     column = @Column(name = "orig_pincode")),
        @AttributeOverride(name = "country",     column = @Column(name = "orig_country"))
    })
    private LocationAddress originAddress;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "locationId", column = @Column(name = "dest_location_id")),
        @AttributeOverride(name = "street",     column = @Column(name = "dest_street")),
        @AttributeOverride(name = "city",        column = @Column(name = "dest_city")),
        @AttributeOverride(name = "state",       column = @Column(name = "dest_state")),
        @AttributeOverride(name = "pincode",     column = @Column(name = "dest_pincode")),
        @AttributeOverride(name = "country",     column = @Column(name = "dest_country"))
    })
    private LocationAddress destinationAddress;

    @Column(name = "cargo_ready_date")
    private LocalDate cargoReadyDate;

    @Column(name = "requested_pickup_date")
    private LocalDate requestedPickupDate;

    @Column(name = "requested_delivery_date")
    private LocalDate requestedDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "load_type", length = 20)
    private LoadType loadType;

    @Column(name = "total_weight_kg", precision = 12, scale = 3)
    private BigDecimal totalWeightKg;

    @Column(name = "total_volume_m3", precision = 12, scale = 4)
    private BigDecimal totalVolumeM3;

    @Column(name = "total_packages")
    private Integer totalPackages;

    @Column(name = "contract_id", length = 100)
    private String contractId;

    @Column(name = "agreed_rate", precision = 12, scale = 2)
    private BigDecimal agreedRate;

    @Column(name = "currency", length = 5)
    @Builder.Default
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private TransportRequestStatus status = TransportRequestStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (trId == null) trId = UUID.randomUUID().toString();
        createdAt = updatedAt = LocalDateTime.now();
        if (status == null) status = TransportRequestStatus.PENDING;
        if (currency == null) currency = "INR";
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
