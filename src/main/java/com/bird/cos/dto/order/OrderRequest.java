package com.bird.cos.dto.order;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Builder
@Getter
@ToString
public class OrderRequest {

    private Long productId;
    private Long productOptionId;
    private Integer quantity;
    private BigDecimal price;
    private Long cartItemId;

    // 기본 setter들
    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setCartItemId(Long cartItemId) {
        this.cartItemId = cartItemId;
    }

    public void setCartItemId(String cartItemIdStr) {
        if (cartItemIdStr == null || cartItemIdStr.trim().isEmpty()) {
            this.cartItemId = null;
            return;
        }
        try {
            this.cartItemId = Long.valueOf(cartItemIdStr.trim());
        } catch (NumberFormatException e) {
            this.cartItemId = null;
        }
    }

    // productOptionId에 대한 custom setter - "default" 문자열을 null로 처리
    public void setProductOptionId(String productOptionIdStr) {
        if (productOptionIdStr == null || productOptionIdStr.trim().isEmpty() || "default".equals(productOptionIdStr)) {
            this.productOptionId = null;
        } else {
            try {
                this.productOptionId = Long.valueOf(productOptionIdStr);
            } catch (NumberFormatException e) {
                this.productOptionId = null;
            }
        }
    }

    // Long 타입으로 직접 설정하는 setter도 유지
    public void setProductOptionId(Long productOptionId) {
        this.productOptionId = productOptionId;
    }
}
