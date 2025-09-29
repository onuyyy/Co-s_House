package com.bird.cos.dto.mypage;

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
public class MyOrderRequest {
    private SearchDate searchDate;      // 기간 검색 (ALL, MONTH_3, MONTH_6, MONTH_12)
    private String orderStatus;         // 주문 상태 (ORDER_001, ORDER_002 등)
    private String searchValue;         // 검색어 (상품명, 옵션명, 브랜드명)
}
