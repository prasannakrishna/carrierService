package com.bhagwat.scm.carrierService.entity;

import com.bhagwat.scm.carrierService.enums.AsnStatus;
import com.bhagwat.scm.carrierService.enums.ShipmentType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "advanced_shipment_notices")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AdvancedShipmentNotice {
    @Id @Column(name = "asn_id", nullable = false, updatable = false)
    private String asnId;

    @Column(name = "asn_number", unique = true, nullable = false, length = 30)
    private String asnNumber;

    @Column(name = "rts_id", length = 100)
    private String rtsId;

    @Column(name = "rts_number", length = 30)
    private String rtsNumber;

    @Column(name = "sent_to_party_id", length = 100)
    private String sentToPartyId;

    @Column(name = "sent_to_party_type", length = 30)
    private String sentToPartyType;

    @Column(name = "sent_to_party_name", length = 255)
    private String sentToPartyName;

    @Column(name = "carrier_id", length = 100)
    private String carrierId;

    @Column(name = "carrier_name", length = 255)
    private String carrierName;

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

    @Column(name = "expected_arrival_date")
    private LocalDate expectedArrivalDate;

    @Column(name = "delivery_window", length = 20)
    private String deliveryWindow;

    @Column(name = "total_weight_kg", precision = 12, scale = 3)
    private BigDecimal totalWeightKg;

    @Column(name = "total_volume_m3", precision = 12, scale = 4)
    private BigDecimal totalVolumeM3;

    @Column(name = "total_packages")
    private Integer totalPackages;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private AsnStatus status = AsnStatus.SENT;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (asnId == null) asnId = UUID.randomUUID().toString();
        createdAt = updatedAt = LocalDateTime.now();
        if (status == null) status = AsnStatus.SENT;
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
