package com.bird.cos.dto.mypage;

import com.bird.cos.domain.product.ProductLike;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class MyLikedProductResponse {

    private final Long likeId;
    private final Long productId;
    private final String productTitle;
    private final String imageUrl;
    private final BigDecimal price;
    private final BigDecimal salePrice;
    private final LocalDateTime likedAt;

    public static MyLikedProductResponse from(ProductLike like) {
        return MyLikedProductResponse.builder()
                .likeId(like.getId())
                .productId(like.getProduct().getProductId())
                .productTitle(like.getProduct().getProductTitle())
                .imageUrl(like.getProduct().getMainImageUrl())
                .price(like.getProduct().getOriginalPrice())
                .salePrice(like.getProduct().getSalePrice())
                .likedAt(like.getCreatedAt())
                .build();
    }
}

