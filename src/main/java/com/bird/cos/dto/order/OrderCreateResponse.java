package com.bird.cos.dto.order;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class OrderCreateResponse {

    private boolean success;
    private String message;
    private Long orderId;
    private BigDecimal totalAmount;
    private List<OrderItemSummary> items;

    @Getter
    @Builder
    public static class OrderItemSummary {
        private Long productId;
        private String productName;
        private String productOptionName;
        private Integer quantity;
        private BigDecimal price;
        private String imageUrl;
    }

    public static OrderCreateResponse success(OrderResponse order) {
        return OrderCreateResponse.builder()
                .success(true)
                .message("주문이 성공적으로 완료되었습니다.")
                .orderId(order.getOrderId())
                .totalAmount(order.getTotalAmount())
                .items(toSummaries(order.getItems()))
                .build();
    }

    public static OrderCreateResponse failure(String message) {
        return OrderCreateResponse.builder()
                .success(false)
                .message(message)
                .build();
    }

    private static List<OrderItemSummary> toSummaries(List<OrderResponse.OrderItemResponse> items) {
        if (items == null) {
            return List.of();
        }

        return items.stream()
                .map(item -> OrderItemSummary.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .productOptionName(item.getProductOptionName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .imageUrl(item.getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }
}
