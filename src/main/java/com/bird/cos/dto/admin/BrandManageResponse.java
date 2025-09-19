package com.bird.cos.dto.admin;

import com.bird.cos.domain.brand.Brand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BrandManageResponse {
    
    private Long brandId;
    private String brandName;
    private String logoUrl;
    private String brandDescription;
    private Integer productCount; // 해당 브랜드의 상품 수
    
    public static BrandManageResponse from(Brand brand) {
        return new BrandManageResponse(
                brand.getBrandId(),
                brand.getBrandName(),
                brand.getLogoUrl(),
                brand.getBrandDescription(),
                brand.getProducts() != null ? brand.getProducts().size() : 0
        );
    }
}
