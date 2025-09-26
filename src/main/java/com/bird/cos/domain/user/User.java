package com.bird.cos.domain.user;

import com.bird.cos.dto.admin.UserUpdateRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "USER")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_role")
    private UserRole userRole;

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

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "user_created_at", insertable = false, updatable = false)
    private LocalDateTime userCreatedAt;

    @Column(name = "user_updated_at", insertable = false, updatable = false)
    private LocalDateTime userUpdatedAt;

    // 사용자 정보 업데이트 메서드
    public void update(UserUpdateRequest request) {
        if (request.getUserName() != null && !request.getUserName().trim().isEmpty()) {
            this.userName = request.getUserName().trim();
        }
        if (request.getUserNickname() != null && !request.getUserNickname().trim().isEmpty()) {
            this.userNickname = request.getUserNickname().trim();
        }
        if (request.getUserEmail() != null && !request.getUserEmail().trim().isEmpty()) {
            this.userEmail = request.getUserEmail().trim();
        }
        if (request.getUserPhone() != null) {
            this.userPhone = request.getUserPhone().trim();
        }
        if (request.getUserAddress() != null) {
            this.userAddress = request.getUserAddress().trim();
        }
        if (request.getTermsAgreed() != null) {
            this.termsAgreed = request.getTermsAgreed();
        }
    }

    // 역할 변경 메서드 (관리자가 사용자 역할을 변경할 때)
    public void changeRole(UserRole newRole) {
        this.userRole = newRole;
    }

    // 관리자 여부 확인
    public boolean isAdmin() {
        return this.userRole != null && 
               ("ADMIN".equals(this.userRole.getUserRoleName()) || 
                "SUPER_ADMIN".equals(this.userRole.getUserRoleName()));
    }

    // 일반 사용자 여부 확인  
    public boolean isUser() {
        return this.userRole != null && "USER".equals(this.userRole.getUserRoleName());
    }


    public String getNickname() { return this.userNickname;
    }

    public void updateEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            this.userEmail = email.trim();
        }
    }

    public void updateNameIfBlank(String name) {
        if ((this.userName == null || this.userName.trim().isEmpty()) && name != null && !name.trim().isEmpty()) {
            this.userName = name.trim();
        }
    }

    public void updateNickname(String nickname) {
        if (nickname != null && !nickname.trim().isEmpty()) {
            this.userNickname = nickname.trim();
        }
    }

    public void linkSocialAccount(String provider, String id) {
        this.socialProvider = provider;
        this.socialId = id;
    }

    public void agreeTerms() {
        this.termsAgreed = true;
    }

    public void markEmailVerified() {
        this.emailVerified = true;
    }

    public void updateUserRole(UserRole newRole) {
        this.userRole = newRole;
    }
}
