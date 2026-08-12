package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pin_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PinHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "operator_id", nullable = false)
    private UUID operatorId;

    @Column(name = "pin_hash", nullable = false)
    private String pinHash;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;
}
