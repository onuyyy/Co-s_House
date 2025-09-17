package com.bird.cos.repository.product;

import com.bird.cos.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // 제품명으로 검색
    Page<Product> findProductsByProductTitleContainingIgnoreCase(String productTitle, Pageable pageable);
    
    // 브랜드명으로 검색
    @Query("SELECT p FROM Product p WHERE p.brand.brandName LIKE %:brandName%")
    Page<Product> findProductsByBrandNameContainingIgnoreCase(@Param("brandName") String brandName, Pageable pageable);
    
    // 카테고리명으로 검색
    @Query("SELECT p FROM Product p WHERE p.productCategory.categoryName LIKE %:categoryName%")
    Page<Product> findProductsByCategoryNameContainingIgnoreCase(@Param("categoryName") String categoryName, Pageable pageable);
    
    // 상품 상태로 검색
    @Query("SELECT p FROM Product p WHERE p.productStatusCode.codeName LIKE %:status%")
    Page<Product> findProductsByStatusContainingIgnoreCase(@Param("status") String status, Pageable pageable);
    
    // 색상으로 검색
    Page<Product> findProductsByProductColorContainingIgnoreCase(String productColor, Pageable pageable);
}
