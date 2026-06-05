package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "integration_configs")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class IntegrationConfig {
    @Id @Column(name = "integration_id", length = 50)
    private String integrationId;
    @Column(name = "system_name", length = 200)
    private String systemName;
    @Column(name = "type", length = 30)
    private String type;
    @Column(name = "direction", length = 30)
    private String direction;
    @Column(name = "last_sync")
    private Instant lastSync;
    @Column(name = "status", length = 30)
    private String status;
}
