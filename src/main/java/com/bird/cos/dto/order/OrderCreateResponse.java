package com.bird.cos.dto.order;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderCreateResponse {

    private boolean success;
    private String message;
    private Long orderId;

    public static OrderCreateResponse success(Long orderId) {
        return OrderCreateResponse.builder()
                .success(true)
                .message("주문이 성공적으로 완료되었습니다.")
                .orderId(orderId)
                .build();
    }

    public static OrderCreateResponse failure(String message) {
        return OrderCreateResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}