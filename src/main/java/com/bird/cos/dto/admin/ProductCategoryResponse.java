package com.bird.cos.dto.admin;

import com.bird.cos.domain.product.ProductCategory;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryResponse {

    private Long productCategoryId;
    private String productCategoryName;
    private String categoryType;

    private ProductCategoryResponse parentCategory;
    private List<ProductCategoryResponse> childCategories = new ArrayList<>();

    public static ProductCategoryResponse from(ProductCategory productCategory) {
        if (productCategory == null) return null;

        return ProductCategoryResponse.builder()
                .productCategoryId(productCategory.getCategoryId())
                .productCategoryName(productCategory.getCategoryName())
                .categoryType(productCategory.getCategoryTypeCode().getCodeName())
                .build();
    }

    public static ProductCategoryResponse fromWithParent(ProductCategory productCategory) {
        if (productCategory == null) return null;

        ProductCategoryResponse response = from(productCategory);

        if (productCategory.getParentCategory() != null) {
            response.setParentCategory(from(productCategory.getParentCategory()));
        }

        return response;
    }

    public static ProductCategoryResponse fromWithChildren(ProductCategory productCategory) {
        if (productCategory == null) return null;

        ProductCategoryResponse response = from(productCategory);

        if (productCategory.getChildCategories() != null && !productCategory.getChildCategories().isEmpty()) {
            List<ProductCategoryResponse> childList = productCategory.getChildCategories().stream()
                    .map(ProductCategoryResponse::from)
                    .toList();
            response.setChildCategories(childList);
        }

        return response;
    }
}

