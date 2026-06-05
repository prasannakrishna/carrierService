package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity @Table(name = "freight_invoices")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FreightInvoice {
    @Id @Column(name = "invoice_id", length = 50)
    private String invoiceId;
    @Column(name = "party", length = 200)
    private String party;
    @Column(name = "amount", precision = 14, scale = 2)
    private BigDecimal amount;
    @Column(name = "type", length = 30)
    private String type;
    @Column(name = "due_date")
    private LocalDate dueDate;
    @Column(name = "status", length = 30)
    private String status;
    @Column(name = "created_at")
    private Instant createdAt;
    @PrePersist void pre() { createdAt = Instant.now(); }
}
