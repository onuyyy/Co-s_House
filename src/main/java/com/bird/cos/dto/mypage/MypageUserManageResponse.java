package com.bird.cos.dto.mypage;

import com.bird.cos.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MypageUserManageResponse {
    private String userName;
    private String userEmail;
    private String userPhone;
    private String userAddress;
    private String userDetailAddress;
    private String userNickname;
    private LocalDateTime userCreatedAt;
    private String socialProvider;
    private String userRole;

    private Integer membershipGrade;
    private String membershipGradeName;
    private BigDecimal totalOrderAmount;
    private Integer membershipPoints;

    public static MypageUserManageResponse from(User user) {
        String address = user.getUserAddress();
        String baseAddress = null;
        String detailAddress = null;

        if (address != null && !address.isBlank()) {
            String[] parts = address.split(",", 2);
            baseAddress = parts[0].trim();
            if (parts.length > 1) {
                detailAddress = parts[1].trim();
            }
        }

        return MypageUserManageResponse.builder()
                .userName(user.getUserName())
                .userEmail(user.getUserEmail())
                .userPhone(user.getUserPhone())
                .userAddress(baseAddress)
                .userDetailAddress(detailAddress)
                .userNickname(user.getUserNickname())
                .userCreatedAt(user.getUserCreatedAt())
                .socialProvider(user.getSocialProvider())
                .userRole(user.getUserRole() != null ? user.getUserRole().getUserRoleName() : null)
                .membershipGrade(null)
                .membershipGradeName(null)
                .totalOrderAmount(BigDecimal.ZERO)
                .membershipPoints(0)
                .build();
    }
}
