package com.bird.cos.repository.product;

import com.bird.cos.domain.product.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
    List<ProductOption> findByProduct_ProductId(Long productId);

    List<ProductOption> findByProductProductId(Long productId);

}
