package com.bhagwat.scm.carrierService.entity;

import com.bhagwat.scm.carrierService.enums.CbrRespStatus;
import com.bhagwat.scm.carrierService.enums.RateType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "carrier_booking_responses")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CarrierBookingResponse {
    @Id @Column(name = "cbr_resp_id", nullable = false, updatable = false)
    private String cbrRespId;

    @Column(name = "cbr_resp_number", unique = true, nullable = false, length = 30)
    private String cbrRespNumber;

    @Column(name = "cbr_id", nullable = false, length = 100)
    private String cbrId;

    @Column(name = "carrier_id", length = 100)
    private String carrierId;

    @Column(name = "carrier_name", length = 255)
    private String carrierName;

    @Column(name = "vehicle_id", length = 100)
    private String vehicleId;

    @Column(name = "offered_pickup_date")
    private LocalDate offeredPickupDate;

    @Column(name = "offered_delivery_date")
    private LocalDate offeredDeliveryDate;

    @Column(name = "rate_offered", precision = 12, scale = 2)
    private BigDecimal rateOffered;

    @Column(name = "currency", length = 5)
    @Builder.Default
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_type", length = 25)
    private RateType rateType;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 25)
    @Builder.Default
    private CbrRespStatus status = CbrRespStatus.PENDING;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (cbrRespId == null) cbrRespId = UUID.randomUUID().toString();
        createdAt = updatedAt = LocalDateTime.now();
        if (status == null) status = CbrRespStatus.PENDING;
        if (currency == null) currency = "INR";
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
