package com.bhagwat.scm.carrierService.entity;

import com.bhagwat.scm.carrierService.enums.CarrierType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "carrier_master")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Carrier {
    @Id @Column(name = "carrier_id", nullable = false, updatable = false)
    private String carrierId;

    @Column(name = "carrier_name", nullable = false, length = 255)
    private String carrierName;

    @Column(name = "scac_code", length = 10)
    private String scacCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "carrier_type", length = 30)
    private CarrierType carrierType;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "locationId", column = @Column(name = "addr_location_id")),
        @AttributeOverride(name = "street",     column = @Column(name = "addr_street")),
        @AttributeOverride(name = "city",        column = @Column(name = "addr_city")),
        @AttributeOverride(name = "state",       column = @Column(name = "addr_state")),
        @AttributeOverride(name = "pincode",     column = @Column(name = "addr_pincode")),
        @AttributeOverride(name = "country",     column = @Column(name = "addr_country"))
    })
    private LocationAddress address;

    @Column(name = "gstin", length = 20)
    private String gstin;

    @Column(name = "pan_number", length = 15)
    private String panNumber;

    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (carrierId == null) carrierId = UUID.randomUUID().toString();
        createdAt = updatedAt = LocalDateTime.now();
        if (active == null) active = true;
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
