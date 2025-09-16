package com.bird.cos.domain.user;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USER")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_email", length = 255, unique = true, nullable = false)
    private String userEmail;

    @Column(name = "user_password", length = 255)
    private String userPassword;

    @Column(name = "user_nickname", length = 20, unique = true, nullable = false)
    private String userNickname;

    @Column(name = "user_name", length = 50, nullable = false)
    private String userName;

    @Column(name = "user_address", columnDefinition = "TEXT")
    private String userAddress;

    @Column(name = "user_phone", length = 20)
    private String userPhone;

    @Column(name = "social_provider", length = 20)
    private String socialProvider;

    @Column(name = "social_id", length = 255)
    private String socialId;

    @Column(name = "terms_agreed")
    private Boolean termsAgreed = false;

    @Column(name = "user_created_at", insertable = false, updatable = false)
    private LocalDateTime userCreatedAt;

    @Column(name = "user_updated_at", insertable = false, updatable = false)
    private LocalDateTime userUpdatedAt;

}