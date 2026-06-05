package com.bhagwat.scm.carrierService.entity;

import com.bhagwat.scm.carrierService.enums.ShipmentType;
import com.bhagwat.scm.carrierService.enums.TransportShipmentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "transport_shipments")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransportShipment {
    @Id @Column(name = "ts_id", nullable = false, updatable = false)
    private String tsId;

    @Column(name = "ts_number", unique = true, nullable = false, length = 30)
    private String tsNumber;

    @Column(name = "rts_id", length = 100)
    private String rtsId;

    @Column(name = "rts_number", length = 30)
    private String rtsNumber;

    @Column(name = "transport_plan_id", length = 100)
    private String transportPlanId;

    @Column(name = "transport_plan_number", length = 30)
    private String transportPlanNumber;

    @Column(name = "carrier_id", length = 100)
    private String carrierId;

    @Column(name = "carrier_name", length = 255)
    private String carrierName;

    @Column(name = "vehicle_id", length = 100)
    private String vehicleId;

    @Column(name = "vehicle_number", length = 30)
    private String vehicleNumber;

    @Column(name = "driver_name", length = 255)
    private String driverName;

    @Column(name = "driver_phone", length = 30)
    private String driverPhone;

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

    @Column(name = "actual_pickup_date_time")
    private LocalDateTime actualPickupDateTime;

    @Column(name = "estimated_delivery_date_time")
    private LocalDateTime estimatedDeliveryDateTime;

    @Column(name = "actual_delivery_date_time")
    private LocalDateTime actualDeliveryDateTime;

    @Column(name = "total_weight_kg", precision = 12, scale = 3)
    private BigDecimal totalWeightKg;

    @Column(name = "total_volume_m3", precision = 12, scale = 4)
    private BigDecimal totalVolumeM3;

    @Column(name = "total_packages")
    private Integer totalPackages;

    @Column(name = "current_location", length = 255)
    private String currentLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 25)
    @Builder.Default
    private TransportShipmentStatus status = TransportShipmentStatus.CREATED;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (tsId == null) tsId = UUID.randomUUID().toString();
        createdAt = updatedAt = LocalDateTime.now();
        if (status == null) status = TransportShipmentStatus.CREATED;
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
