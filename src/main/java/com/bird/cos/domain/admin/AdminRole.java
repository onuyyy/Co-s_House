package com.bird.cos.domain.admin;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ADMIN_ROLE")
public class AdminRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_role_id")
    private Long adminRoleId;

    @Column(name = "admin_role_name", length = 50, nullable = false)
    private String adminRoleName;

    @Column(name = "role_description", length = 200)
    private String roleDescription;

    @Column(name = "role_created_date", nullable = false, insertable = false, updatable = false)
    private LocalDateTime roleCreatedDate;

    @Column(name = "role_updated_date")
    private LocalDateTime roleUpdatedDate;

    @OneToMany(mappedBy = "adminRole", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Admin> admins = new ArrayList<>();

}