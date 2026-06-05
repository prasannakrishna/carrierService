package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "sites")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Site {
    @Id @Column(name = "site_id", length = 50)
    private String siteId;
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    @Column(name = "type", length = 50)
    private String type;
    @Column(name = "city", length = 100)
    private String city;
    @Column(name = "capacity", length = 50)
    private String capacity;
    @Column(name = "status", length = 30)
    private String status;
}
