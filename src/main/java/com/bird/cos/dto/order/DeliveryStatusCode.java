package com.bird.cos.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DeliveryStatusCode {

    PREPARING("DELIVERY_001", "배송 준비"),
    SHIPPED("DELIVERY_002", "배송 출발"),
    IN_TRANSIT("DELIVERY_003", "배송중"),
    DELIVERED("DELIVERY_004", "배송 완료"),
    FAILED("DELIVERY_005", "배송 실패");

    private final String code;
    private final String description;
}
