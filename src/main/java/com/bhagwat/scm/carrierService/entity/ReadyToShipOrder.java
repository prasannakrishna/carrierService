package com.bhagwat.scm.carrierService.entity;

import com.bhagwat.scm.carrierService.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "ready_to_ship_orders")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReadyToShipOrder {
    @Id @Column(name = "rts_id", nullable = false, updatable = false)
    private String rtsId;

    @Column(name = "rts_number", unique = true, nullable = false, length = 30)
    private String rtsNumber;

    @Column(name = "tr_id", length = 100)
    private String trId;

    @Column(name = "cbr_id", length = 100)
    private String cbrId;

    @Column(name = "carrier_id", length = 100)
    private String carrierId;

    @Column(name = "carrier_name", length = 255)
    private String carrierName;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipment_type", length = 30)
    private ShipmentType shipmentType;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "partyId",      column = @Column(name = "shp_party_id")),
        @AttributeOverride(name = "partyName",    column = @Column(name = "shp_party_name")),
        @AttributeOverride(name = "partyType",    column = @Column(name = "shp_party_type")),
        @AttributeOverride(name = "orgId",        column = @Column(name = "shp_org_id")),
        @AttributeOverride(name = "contactPhone", column = @Column(name = "shp_contact_phone"))
    })
    private ShipmentParty shipper;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "partyId",      column = @Column(name = "con_party_id")),
        @AttributeOverride(name = "partyName",    column = @Column(name = "con_party_name")),
        @AttributeOverride(name = "partyType",    column = @Column(name = "con_party_type")),
        @AttributeOverride(name = "orgId",        column = @Column(name = "con_org_id")),
        @AttributeOverride(name = "contactPhone", column = @Column(name = "con_contact_phone"))
    })
    private ShipmentParty consignee;

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

    @Column(name = "cargo_ready_date_time")
    private LocalDateTime cargoReadyDateTime;

    @Column(name = "cargo_cutoff_date_time")
    private LocalDateTime cargoCutoffDateTime;

    @Column(name = "document_cutoff_date_time")
    private LocalDateTime documentCutoffDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "load_type", length = 20)
    private LoadType loadType;

    @Column(name = "total_weight_kg", precision = 12, scale = 3)
    private BigDecimal totalWeightKg;

    @Column(name = "total_volume_m3", precision = 12, scale = 4)
    private BigDecimal totalVolumeM3;

    @Column(name = "total_packages")
    private Integer totalPackages;

    @Column(name = "incoterm", length = 10)
    private String incoterm;

    @Column(name = "freight_payment_code", length = 20)
    private String freightPaymentCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private RtsStatus status = RtsStatus.DRAFT;

    @Column(name = "asn_sent")
    @Builder.Default
    private Boolean asnSent = false;

    @Column(name = "asn_sent_at")
    private LocalDateTime asnSentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (rtsId == null) rtsId = UUID.randomUUID().toString();
        createdAt = updatedAt = LocalDateTime.now();
        if (status == null) status = RtsStatus.DRAFT;
        if (asnSent == null) asnSent = false;
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
