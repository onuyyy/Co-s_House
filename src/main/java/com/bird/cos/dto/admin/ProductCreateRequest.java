package com.bird.cos.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductCreateRequest {

    private String productTitle;
    private Long brandId;
    private Long productCategoryId;
    private String mainImageUrl;
    private String description;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private BigDecimal couponPrice;
    private BigDecimal discountRate;
    private Boolean isFreeShipping;
    private Boolean isTodayDeal;
    private Boolean isCohouseOnly;
    private String productColor;
    private String material;
    private String capacity;
    private Integer stockQuantity;
    private String productStatusCodeId;
}
