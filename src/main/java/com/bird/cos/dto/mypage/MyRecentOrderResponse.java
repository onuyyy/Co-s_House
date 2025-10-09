package com.bird.cos.dto.mypage;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyRecentOrderResponse {

    private final Long orderId;
    private final String orderNumber;
    private final LocalDateTime orderDate;

    public static MyRecentOrderResponse of(Long orderId, LocalDateTime orderDate) {
        String formattedNumber = orderId != null ? String.format("ORD-%06d", orderId) : "ORD-000000";
        return MyRecentOrderResponse.builder()
                .orderId(orderId)
                .orderDate(orderDate)
                .orderNumber(formattedNumber)
                .build();
    }
}

