package com.bird.cos.repository;

import com.bird.cos.domain.proudct.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
  /*  *미구현
        findByProductCategoryCategoryId(Long categoryId)
      - 목적: 특정 카테고리에 속한 모든 상품을 조회
      - 매개변수: categoryId - 조회할 카테고리의 ID
      - 반환값: 해당 카테고리의 상품 리스트
      - 쿼리: SELECT * FROM PRODUCT WHERE product_category_id = ?
  */
    List<Product> findByProductCategoryCategoryId(Long categoryId);

  /*  *미구현
        findByProductCategoryCategoryIdOrderBySalePriceAsc(Long categoryId)
        - 목적: 특정 카테고리의 상품을 세일가격 기준 오름차순으로 조회 (낮은 가격순)
        - 매개변수: categoryId - 조회할 카테고리의 ID
        - 반환값: 세일가격 낮은 순으로 정렬된 상품 리스트
        - 쿼리: SELECT * FROM PRODUCT WHERE product_category_id = ?
             ORDER BY sale_price ASC
  */
    List<Product> findByProductCategoryCategoryIdOrderBySalePriceAsc(Long categoryId);

  /*  *미구현
        findByProductCategoryCategoryIdOrderBySalePriceDesc(Long categoryId)
        - 목적: 특정 카테고리의 상품을 세일가격 기준 내림차순으로 조회 (높은 가격순)
        - 매개변수: categoryId - 조회할 카테고리의 ID
        - 반환값: 세일가격 높은 순으로 정렬된 상품 리스트
        - 쿼리: SELECT * FROM PRODUCT WHERE product_category_id = ?
             ORDER BY sale_price DESC
  */
    List<Product> findByProductCategoryCategoryIdOrderBySalePriceDesc(Long categoryId);

      /*
        findByProductCategoryCategoryIdOrderByAverageRatingDesc(Long categoryId)
        - 목적: 특정 카테고리의 상품을 평균평점 기준 내림차순으로 조회 (평점 높은순)
        - 매개변수: categoryId - 조회할 카테고리의 ID
        - 반환값: 평점 높은 순으로 정렬된 상품 리스트
        - 쿼리: SELECT * FROM PRODUCT WHERE product_category_id = ?
                 ORDER BY average_rating DESC
       */
    List<Product> findByProductCategoryCategoryIdOrderByAverageRatingDesc(Long categoryId);
}