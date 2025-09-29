package com.bird.cos.dto.mypage;

import com.bird.cos.domain.user.PointType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPointRequest {
    private SearchDate period;      // 기간 검색 (ALL, MONTH_1, MONTH_3, MONTH_6, YEAR_1)
    private String type;            // 포인트 타입 (ALL, EARN, USE)
}
