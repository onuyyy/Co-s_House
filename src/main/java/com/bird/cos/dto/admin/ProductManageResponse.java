package com.bird.cos.dto.admin;

import com.bird.cos.domain.product.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductManageResponse {
    
    private Long productId;
    private String productTitle;
    private String brandName;
    private String categoryName;
    private String mainImageUrl;
    private String description;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private BigDecimal couponPrice;
    private BigDecimal discountRate;
    private String productColor;
    private String material;
    private String capacity;
    private Integer stockQuantity;
    private Long viewCount;
    private Long salesCount;
    private Integer reviewCount;
    private BigDecimal averageRating;
    private String productStatus;
    private Boolean isTodayDeal;
    private Boolean isFreeShipping;
    private Boolean isCohouseOnly;
    private LocalDateTime productCreatedAt;
    private LocalDateTime productUpdatedAt;
    
    public static ProductManageResponse from(Product product) {
        return new ProductManageResponse(
                product.getProductId(),
                product.getProductTitle(),
                product.getBrand() != null ? product.getBrand().getBrandName() : null,
                product.getProductCategory() != null ? product.getProductCategory().getCategoryName() : null,
                product.getMainImageUrl(),
                product.getDescription(),
                product.getOriginalPrice(),
                product.getSalePrice(),
                product.getCouponPrice(),
                product.getDiscountRate(),
                product.getProductColor(),
                product.getMaterial(),
                product.getCapacity(),
                product.getStockQuantity(),
                product.getViewCount(),
                product.getSalesCount(),
                product.getReviewCount(),
                product.getAverageRating(),
                product.getProductStatusCode() != null ? product.getProductStatusCode().getCodeName() : null,
                product.getIsTodayDeal(),
                product.getIsFreeShipping(),
                product.getIsCohouseOnly(),
                product.getProductCreatedAt(),
                product.getProductUpdatedAt()
        );
    }
}
