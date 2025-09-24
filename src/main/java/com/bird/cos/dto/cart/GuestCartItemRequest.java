package com.bird.cos.dto.cart;

public record GuestCartItemRequest(Long productId, Integer quantity, String selectedOptions) {}

