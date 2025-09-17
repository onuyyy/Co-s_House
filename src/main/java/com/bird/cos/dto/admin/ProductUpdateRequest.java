package com.bird.cos.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {
    
    private String productTitle;
    private String description;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private BigDecimal couponPrice;
    private BigDecimal discountRate;
    private String productColor;
    private String material;
    private String capacity;
    private Integer stockQuantity;
    private Boolean isFreeShipping;
    private Boolean isTodayDeal;
    private Boolean isCohouseOnly;
    private String mainImageUrl;
}
