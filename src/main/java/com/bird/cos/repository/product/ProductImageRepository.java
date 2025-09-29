package com.bird.cos.repository.product;

import com.bird.cos.domain.product.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    // ProductId를 기준으로 모든 ProductImage를 찾기
    List<ProductImage> findByProduct_ProductId(Long productId);
}