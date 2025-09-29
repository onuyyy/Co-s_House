package com.bird.cos.repository.product;

import com.bird.cos.domain.product.ProductOption;
import io.micrometer.common.KeyValues;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
    List<ProductOption> findByProduct_ProductId(Long productId);

    List<ProductOption> findByProductProductId(Long productId);

    List<ProductOption> findByProduct_ProductIdIn(Set<Long> productIds);
}
