package com.bird.cos.dto.cart;

public record AddToCartRequest(Long productId, Integer quantity, String selectedOptions) {}

