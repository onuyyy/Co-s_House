package com.bird.cos.domain.user;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "USER_ROLE")
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_role_id")
    private Long userRoleId;

    @Column(name = "user_role_name", length = 50, nullable = false)
    private String userRoleName;

    @Column(name = "role_description", length = 200)
    private String roleDescription;

    @Column(name = "role_created_date", nullable = false, insertable = false, updatable = false)
    private LocalDateTime roleCreatedDate;

    @Column(name = "role_updated_date")
    private LocalDateTime roleUpdatedDate;


}
