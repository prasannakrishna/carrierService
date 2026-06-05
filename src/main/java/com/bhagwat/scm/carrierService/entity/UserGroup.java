package com.bhagwat.scm.carrierService.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "user_groups")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserGroup {
    @Id @Column(name = "group_id", length = 50)
    private String groupId;
    @Column(name = "group_name", nullable = false, length = 200)
    private String groupName;
    @Column(name = "permissions", length = 500)
    private String permissions;
    @Column(name = "member_count")
    private Integer memberCount;
    @Column(name = "status", length = 30)
    private String status;
}
