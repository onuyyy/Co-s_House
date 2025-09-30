package com.bird.cos.repository.product;

import com.bird.cos.domain.product.Product;
import com.bird.cos.service.home.dto.HomeProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("select new com.bird.cos.service.home.dto.HomeProductDto(p.productId, p.productTitle, p.mainImageUrl, p.originalPrice, p.salePrice, p.discountRate, p.averageRating, p.reviewCount) " +
            "from Product p where p.isTodayDeal = true order by p.discountRate desc nulls last, p.salesCount desc")
    List<HomeProductDto> findTodayDeals(Pageable pageable);

    @Query("select new com.bird.cos.service.home.dto.HomeProductDto(p.productId, p.productTitle, p.mainImageUrl, p.originalPrice, p.salePrice, p.discountRate, p.averageRating, p.reviewCount) " +
            "from Product p order by p.salesCount desc, p.viewCount desc")
    List<HomeProductDto> findPopular(Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.options WHERE p.productId = :productId")
    Optional<Product> findByIdWithOptions(@Param("productId") Long productId);

    //특정 카테고리에 속한 모든 상품을 조회
    //SELECT * FROM PRODUCT WHERE product_category_id = ?
    List<Product> findByProductCategory_CategoryId(Long categoryId);

    //특정 카테고리의 상품을 가격 기준 오름차순으로 조회 (낮은 가격순)
    //SELECT * FROM PRODUCT WHERE product_category_id = ? ORDER BY sale_price ASC
    List<Product> findByProductCategoryCategoryIdOrderBySalePriceAsc(Long categoryId);

    //특정 카테고리의 상품을 가격 기준 내림차순으로 조회 (높은 가격순)
    //SELECT * FROM PRODUCT WHERE product_category_id = ? ORDER BY sale_price ASC
    List<Product> findByProductCategoryCategoryIdOrderBySalePriceDesc(Long categoryId);


    // 특정 카테고리의 상품을 평균평점 기준 내림차순으로 조회 (평점 높은순)
    // SELECT * FROM PRODUCT WHERE product_category_id = ? ORDER BY average_rating DESC
    List<Product> findByProductCategoryCategoryIdOrderByAverageRatingDesc(Long categoryId);

    List<Product> findByBrand_BrandId(Long brandId);

    List<Product> findByBrand_BrandIdOrderBySalePriceAsc(Long brandId);

    List<Product> findByBrand_BrandIdOrderBySalePriceDesc(Long brandId);

    List<Product> findByBrand_BrandIdOrderByAverageRatingDesc(Long brandId);

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

    // 같은 브랜드, 같은 이름 존재하는지 검색
    Boolean existsByProductTitleAndBrand_BrandId(String productTitle, Long brandId);

}
