package com.bird.cos.service.product;

import com.bird.cos.domain.proudct.Product;
import com.bird.cos.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // DB에 있는 모든 상품을 조회하는 메서드
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    //전체 상품 개수 카운트
    public long getTotalCount() {
        return productRepository.count();
    }

    //특정 상품 ID로 단일 상품 조회
    public Optional<Product> getProductById(Long productId) {
        return productRepository.findById(productId);
    }

    //특정 카테고리의 상품들만 가져오는 메서드(추천순, 최신순, 인기순) *미구현
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByProductCategoryCategoryId(categoryId);
    }

    //특정 카테고리의 상품을 가격순으로 조회(낮은 가격순, 높은 가격순) *미구현
    public List<Product> getProductsSortedByPrice(Long categoryId, boolean ascending) {
        if (ascending) {
            return productRepository.findByProductCategoryCategoryIdOrderBySalePriceAsc(categoryId);
        } else {
            return productRepository.findByProductCategoryCategoryIdOrderBySalePriceDesc(categoryId);
        }
    }

    //특정 카테고리 평점 높은 순으로 조회 *미구현
    public List<Product> getProductsSortedByRating(Long categoryId) {
        return productRepository.findByProductCategoryCategoryIdOrderByAverageRatingDesc(categoryId);
    }

}