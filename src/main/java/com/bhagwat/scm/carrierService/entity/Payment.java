package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name = "payments")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Payment {
    @Id @Column(name = "payment_id", length = 50)
    private String paymentId;
    @Column(name = "invoice_id", length = 50)
    private String invoiceId;
    @Column(name = "party", length = 200)
    private String party;
    @Column(name = "amount", precision = 14, scale = 2)
    private BigDecimal amount;
    @Column(name = "method", length = 50)
    private String method;
    @Column(name = "status", length = 30)
    private String status;
    @Column(name = "created_at")
    private Instant createdAt;
    @PrePersist void pre() { createdAt = Instant.now(); }
}
