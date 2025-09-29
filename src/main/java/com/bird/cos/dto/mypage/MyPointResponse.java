package com.bird.cos.dto.mypage;

import com.bird.cos.domain.user.PointType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPointResponse {
    private Long pointHistoryId;
    private PointType pointType;         // EARN, USE, EXPIRE
    private Integer points;              // 변동 포인트 (절대값)
    private Integer balance;             // 잔액
    private String description;          // 설명
    private LocalDateTime createdAt;     // 생성일시
    private String referenceId;          // 참조 ID
    private String referenceType;        // 참조 타입
}
