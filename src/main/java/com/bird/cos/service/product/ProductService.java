package com.bird.cos.service.product;

import com.bird.cos.domain.product.Product;
import com.bird.cos.repository.product.ProductRepository;
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
        return productRepository.findByIdWithOptions(productId);
    }

    //카테고리 별로 조회
    public List<Product> getProductsByCategory(Long categoryId) {
        List<Product> products = productRepository.findByProductCategory_CategoryId(categoryId);
        return products;
    }

    //세일가격 기준 오름차순으로 조회
    public List<Product> getProductsByCategoryOrderBySalePriceAsc(Long categoryId) {
        List<Product> products = productRepository.findByProductCategoryCategoryIdOrderBySalePriceAsc(categoryId);
        return products;
    }

    //세일가격 기준 내림차순으로 조회
    public List<Product> getProductsByCategoryOrderBySalePriceDesc(Long categoryId) {
        List<Product> products = productRepository.findByProductCategoryCategoryIdOrderBySalePriceDesc(categoryId);
        return products;
    }

    //별점 기준 내림차순으로 조회
    public List<Product> getProductsByCategoryOrderByRatingDesc(Long categoryId) {
        List<Product> products = productRepository.findByProductCategoryCategoryIdOrderByAverageRatingDesc(categoryId);
        return products;
    }

    //브랜드 페이지 조회
    // 브랜드 ID로 상품 목록 조회 (기본)
    public List<Product> getProductsByBrandId(Long brandId) {
        return productRepository.findByBrand_BrandId(brandId);
    }

    // 브랜드 ID로 상품 목록을 '세일 가격' 오름차순으로 조회
    public List<Product> getProductsByBrandOrderBySalePriceAsc(Long brandId) {
        return productRepository.findByBrand_BrandIdOrderBySalePriceAsc(brandId);
    }

    // 브랜드 ID로 상품 목록을 '세일 가격' 내림차순으로 조회
    public List<Product> getProductsByBrandOrderBySalePriceDesc(Long brandId) {
        return productRepository.findByBrand_BrandIdOrderBySalePriceDesc(brandId);
    }

    // 브랜드 ID로 상품 목록을 '세일 가격' 내림차순으로 조회
    public List<Product> getProductsByBrandOrderByRatingDesc(Long brandId) {
        return productRepository.findByBrand_BrandIdOrderByAverageRatingDesc(brandId);
    }

}