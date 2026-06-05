package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "parties")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Party {
    @Id @Column(name = "party_id", length = 50)
    private String partyId;
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    // SOURCE or DESTINATION
    @Column(name = "party_role", length = 30)
    private String partyRole;
    // Warehouse, Factory, Depot, Store
    @Column(name = "type", length = 50)
    private String type;
    @Column(name = "city", length = 100)
    private String city;
    @Column(name = "contact", length = 50)
    private String contact;
    @Column(name = "status", length = 30)
    private String status;
    @Column(name = "created_at")
    private Instant createdAt;
    @PrePersist void pre() { createdAt = Instant.now(); }
}
