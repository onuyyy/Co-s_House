package com.bird.cos.repository.product;

import com.bird.cos.domain.product.ProductCategory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends CrudRepository<ProductCategory, Long> {
    List<ProductCategory> findAllByLevel(Integer level);
    List<ProductCategory> findByParentCategory_CategoryId(Long parentCategoryId);
}
