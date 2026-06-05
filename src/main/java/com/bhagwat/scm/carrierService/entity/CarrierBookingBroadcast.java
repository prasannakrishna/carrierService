package com.bhagwat.scm.carrierService.entity;

import com.bhagwat.scm.carrierService.enums.BroadcastStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "carrier_booking_broadcasts")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CarrierBookingBroadcast {
    @Id @Column(name = "broadcast_id", nullable = false, updatable = false)
    private String broadcastId;

    @Column(name = "cbr_id", nullable = false, length = 100)
    private String cbrId;

    @Column(name = "carrier_id", nullable = false, length = 100)
    private String carrierId;

    @Column(name = "carrier_name", length = 255)
    private String carrierName;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private BroadcastStatus status = BroadcastStatus.SENT;

    @PrePersist
    protected void onCreate() {
        if (broadcastId == null) broadcastId = UUID.randomUUID().toString();
        if (sentAt == null) sentAt = LocalDateTime.now();
        if (status == null) status = BroadcastStatus.SENT;
    }
}
