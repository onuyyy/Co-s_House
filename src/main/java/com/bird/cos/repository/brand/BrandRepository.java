package com.bird.cos.repository.brand;

import com.bird.cos.domain.brand.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    // 브랜드명으로 검색
    Optional<Brand> findByBrandName(String brandName);

    // 브랜드명에 키워드가 포함된 브랜드들 검색
    List<Brand> findByBrandNameContainingIgnoreCase(String keyword);

    // 브랜드 설명에 키워드가 포함된 브랜드들 검색
    List<Brand> findByBrandDescriptionContainingIgnoreCase(String keyword);

    // 상품이 있는 브랜드들만 검색 (JPQL 사용)
    @Query("SELECT DISTINCT b FROM Brand b JOIN b.products p")
    List<Brand> findBrandsWithProducts();

    // 특정 브랜드의 상품 개수 조회
    @Query("SELECT COUNT(p) FROM Product p WHERE p.brand.brandId = :brandId")
    Long countProductsByBrandId(@Param("brandId") Long brandId);

}
