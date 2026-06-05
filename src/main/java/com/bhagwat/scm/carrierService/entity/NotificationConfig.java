package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "notification_configs")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationConfig {
    @Id @Column(name = "config_id", length = 50)
    private String configId;
    @Column(name = "event", length = 200)
    private String event;
    @Column(name = "channel", length = 100)
    private String channel;
    @Column(name = "recipients", length = 255)
    private String recipients;
    @Column(name = "trigger_rule", length = 255)
    private String triggerRule;
    @Column(name = "status", length = 30)
    private String status;
}
