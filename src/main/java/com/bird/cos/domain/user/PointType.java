package com.bird.cos.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 포인트 변동 유형
 * EARN: 포인트 적립 (양수)
 * USE: 포인트 사용 (음수)
 * EXPIRE: 포인트 만료 (음수)
 */
@Getter
@RequiredArgsConstructor
public enum PointType {

    EARN("EARN", "포인트 적립", 1),
    USE("USE", "포인트 사용", -1),
    EXPIRE("EXPIRE", "포인트 만료", -1);

    private final String code;
    private final String description;
    private final int sign; // 1: 증가, -1: 감소

    /**
     * 포인트 타입이 포인트 증가 타입인지 확인
     */
    public boolean isIncrease() {
        return this.sign > 0;
    }

    /**
     * 포인트 타입이 포인트 감소 타입인지 확인
     */
    public boolean isDecrease() {
        return this.sign < 0;
    }

    /**
     * 실제 변동 금액 계산 (타입에 따라 부호 적용)
     */
    public int calculateAmount(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("포인트 금액은 양수여야 합니다: " + amount);
        }
        return amount * this.sign;
    }

    /**
     * 코드로 PointType 찾기
     */
    public static PointType fromCode(String code) {
        for (PointType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("잘못된 포인트 타입 코드: " + code);
    }
}