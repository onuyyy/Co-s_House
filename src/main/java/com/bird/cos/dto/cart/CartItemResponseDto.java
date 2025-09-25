package com.bird.cos.dto.cart;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

// 장바구니 아이템 응답 DTO - 단가/최종가/라인합계 등 표시용 필드 포함
@Getter
@Builder
public class CartItemResponseDto {
    private Long cartItemId;
    private Long productId;
    private String title;
    private String imageUrl;
    private Integer quantity;

    private BigDecimal unitPrice; //원가
    private BigDecimal finalPrice; //최종 단가(할인/쿠폰 적용 후)
    private BigDecimal lineTotal; //라인 합계 = finalPrice * quantity
    private Boolean outOfStock; //품절/재고부족 여부
    private String status; //상품 상태 코드명
}
