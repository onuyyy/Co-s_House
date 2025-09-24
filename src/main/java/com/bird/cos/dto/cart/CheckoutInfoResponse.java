package com.bird.cos.dto.cart;

import java.util.List;

public record CheckoutInfoResponse(
        String userName,
        String userPhone,
        String userAddress,
        List<CheckoutItem> items
) {
    public record CheckoutItem(Long productId, String title, Integer quantity) {}
}

