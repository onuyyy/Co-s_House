package com.bird.cos.dto.mypage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPointSummary {
    private Integer currentPoint;    // 현재 보유 포인트
    private Integer monthEarned;     // 이번 달 적립 포인트
    private Integer monthUsed;       // 이번 달 사용 포인트
    private Integer expiringPoint;   // 소멸 예정 포인트
}
