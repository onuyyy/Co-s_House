package com.bird.cos.repository.brand;

import com.bird.cos.domain.brand.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    
    // 브랜드명으로 검색
    Page<Brand> findBrandsByBrandNameContainingIgnoreCase(String brandName, Pageable pageable);
    
    // 브랜드 설명으로 검색
    Page<Brand> findBrandsByBrandDescriptionContainingIgnoreCase(String brandDescription, Pageable pageable);
    
    // 브랜드명 중복 체크
    boolean existsByBrandNameIgnoreCase(String brandName);
}
