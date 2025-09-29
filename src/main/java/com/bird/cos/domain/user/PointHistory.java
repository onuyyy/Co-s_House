package com.bird.cos.domain.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 포인트 변동 내역 엔티티 (User와 N:1 관계)
 * 모든 포인트 적립/사용/만료 내역을 추적
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "POINT_HISTORY", indexes = {
    @Index(name = "idx_point_history_user_id", columnList = "user_id"),
    @Index(name = "idx_point_history_created_at", columnList = "created_at"),
    @Index(name = "idx_point_history_type", columnList = "type"),
    @Index(name = "idx_point_history_reference_id", columnList = "reference_id")
})
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_history_id")
    private Long pointHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 포인트 변동 유형 (EARN/USE/EXPIRE)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private PointType type;

    /**
     * 변동 포인트 금액 (양수: 증가, 음수: 감소)
     */
    @Column(name = "amount", nullable = false)
    private Integer amount;

    /**
     * 해당 시점의 포인트 잔액 (변동 후 잔액)
     */
    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    /**
     * 변동 전 포인트 잔액
     */
    @Column(name = "balance_before", nullable = false)
    private Integer balanceBefore;

    /**
     * 포인트 변동 사유 설명
     */
    @Column(name = "description", length = 500, nullable = false)
    private String description;

    /**
     * 참조 ID (주문번호, 이벤트ID 등)
     */
    @Column(name = "reference_id", length = 100)
    private String referenceId;

    /**
     * 참조 타입 (ORDER, EVENT, ADMIN 등)
     */
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    /**
     * 생성 시간
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // == 정적 팩토리 메서드 ==

    /**
     * 포인트 적립 내역 생성
     * @param user 사용자
     * @param amount 적립 포인트 (양수)
     * @param balanceBefore 변동 전 잔액
     * @param balanceAfter 변동 후 잔액
     * @param description 설명
     * @param referenceId 참조 ID
     * @param referenceType 참조 타입
     * @return PointHistory 엔티티
     */
    public static PointHistory createEarn(User user, int amount, int balanceBefore, int balanceAfter,
                                         String description, String referenceId, String referenceType) {
        validateAmount(amount, PointType.EARN);

        return PointHistory.builder()
                .user(user)
                .type(PointType.EARN)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .description(description)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();
    }

    /**
     * 포인트 사용 내역 생성
     * @param user 사용자
     * @param amount 사용 포인트 (양수)
     * @param balanceBefore 변동 전 잔액
     * @param balanceAfter 변동 후 잔액
     * @param description 설명
     * @param referenceId 참조 ID
     * @param referenceType 참조 타입
     * @return PointHistory 엔티티
     */
    public static PointHistory createUse(User user, int amount, int balanceBefore, int balanceAfter,
                                        String description, String referenceId, String referenceType) {
        validateAmount(amount, PointType.USE);

        return PointHistory.builder()
                .user(user)
                .type(PointType.USE)
                .amount(-amount) // 사용은 음수로 저장
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .description(description)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();
    }

    /**
     * 포인트 만료 내역 생성
     * @param user 사용자
     * @param amount 만료 포인트 (양수)
     * @param balanceBefore 변동 전 잔액
     * @param balanceAfter 변동 후 잔액
     * @param description 설명
     * @param referenceId 참조 ID
     * @param referenceType 참조 타입
     * @return PointHistory 엔티티
     */
    public static PointHistory createExpire(User user, int amount, int balanceBefore, int balanceAfter,
                                           String description, String referenceId, String referenceType) {
        validateAmount(amount, PointType.EXPIRE);

        return PointHistory.builder()
                .user(user)
                .type(PointType.EXPIRE)
                .amount(-amount) // 만료는 음수로 저장
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .description(description)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();
    }

    /**
     * 포인트 금액 유효성 검증
     */
    private static void validateAmount(int amount, PointType type) {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                String.format("%s 포인트는 양수여야 합니다: %d", type.getDescription(), amount)
            );
        }
    }

    // == 비즈니스 메서드 ==

    /**
     * 포인트 증가 내역인지 확인
     */
    public boolean isIncrease() {
        return this.type.isIncrease();
    }

    /**
     * 포인트 감소 내역인지 확인
     */
    public boolean isDecrease() {
        return this.type.isDecrease();
    }

    /**
     * 실제 변동된 포인트 금액 (절댓값)
     */
    public int getAbsoluteAmount() {
        return Math.abs(this.amount);
    }
}