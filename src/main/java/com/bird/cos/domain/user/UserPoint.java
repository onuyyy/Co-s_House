package com.bird.cos.domain.user;

import com.bird.cos.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "USER_POINT", uniqueConstraints = {
    @UniqueConstraint(columnNames = "user_id")
})
public class UserPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_point_id")
    private Long userPointId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * 총 적립 포인트 (모든 적립 포인트의 합계)
     */
    @Column(name = "total_point", nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private Integer totalPoint = 0;

    /**
     * 현재 사용 가능한 포인트 (총 적립 - 사용 - 만료)
     */
    @Column(name = "available_point", nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private Integer availablePoint = 0;

    /**
     * 최종 업데이트 시간
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 포인트 적립
     * @param amount 적립할 포인트 (양수)
     */
    public void earnPoints(int amount) {
        if (amount <= 0) {
            throw BusinessException.pointInvalidAmount(amount);
        }
        this.totalPoint += amount;
        this.availablePoint += amount;
    }

    /**
     * 포인트 사용
     * @param amount 사용할 포인트 (양수)
     * @throws BusinessException 사용 가능한 포인트가 부족한 경우
     */
    public void usePoints(int amount) {
        if (amount <= 0) {
            throw BusinessException.pointInvalidAmount(amount);
        }
        if (this.availablePoint < amount) {
            throw BusinessException.pointInsufficient(this.user.getUserId(), amount, this.availablePoint);
        }
        this.availablePoint -= amount;
    }

    /**
     * 포인트 만료
     * @param amount 만료될 포인트 (양수)
     */
    public void expirePoints(int amount) {
        if (amount <= 0) {
            throw BusinessException.pointInvalidAmount(amount);
        }
        if (this.availablePoint < amount) {
            // 만료의 경우 보유 포인트보다 많이 만료될 수 있으므로 0으로 조정
            this.availablePoint = 0;
        } else {
            this.availablePoint -= amount;
        }
    }

    /**
     * 사용 가능한 포인트가 충분한지 확인
     * @param amount 확인할 포인트
     * @return 사용 가능 여부
     */
    public boolean canUse(int amount) {
        return this.availablePoint >= amount;
    }

    /**
     * 사용자별 UserPoint 엔티티 생성
     * @param user 사용자
     * @return UserPoint 엔티티
     */
    public static UserPoint createForUser(User user) {
        return UserPoint.builder()
                .user(user)
                .totalPoint(0)
                .availablePoint(0)
                .build();
    }
}