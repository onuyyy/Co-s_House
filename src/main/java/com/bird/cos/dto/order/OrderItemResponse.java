package com.bird.cos.dto.order;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderItemResponse {
    private Long productId;
    private String productTitle;
    private String mainImageUrl;
    private BigDecimal originalPrice;
}
