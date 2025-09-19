package com.bird.cos.dto.cart;

import java.util.List;

public record CartListResponse(List<CartItemResponseDto> items, CartSummaryDto summary) {}

