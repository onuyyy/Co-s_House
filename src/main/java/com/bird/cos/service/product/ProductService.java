package com.bird.cos.service.product;

import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.product.ProductCategory;
import com.bird.cos.domain.product.ProductImage;
import com.bird.cos.domain.product.ProductOption;
import com.bird.cos.repository.product.ProductImageRepository;
import com.bird.cos.repository.product.ProductOptionRepository;
import com.bird.cos.repository.product.ProductRepository;
import com.bird.cos.repository.product.ProductCategoryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private static final Sort DEFAULT_CATEGORY_SORT = Sort.by(Sort.Direction.DESC, "productCreatedAt")
            .and(Sort.by(Sort.Direction.DESC, "productId"));

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

    //카테고리 별로 조회 (기본 정렬)
    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategory(Long categoryId, int page, int size) {
        Pageable pageable = buildCategoryPageable(page, size, Sort.unsorted());
        return productRepository.findByProductCategory_CategoryId(categoryId, pageable);
    }

    //세일가격 기준 오름차순으로 조회
    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategoryOrderBySalePriceAsc(Long categoryId, int page, int size) {
        Pageable pageable = buildCategoryPageable(page, size,
                Sort.by(Sort.Direction.ASC, "salePrice"));
        return productRepository.findByProductCategory_CategoryId(categoryId, pageable);
    }

    //세일가격 기준 내림차순으로 조회
    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategoryOrderBySalePriceDesc(Long categoryId, int page, int size) {
        Pageable pageable = buildCategoryPageable(page, size,
                Sort.by(Sort.Direction.DESC, "salePrice"));
        return productRepository.findByProductCategory_CategoryId(categoryId, pageable);
    }

    //별점 기준 내림차순으로 조회
    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategoryOrderByRatingDesc(Long categoryId, int page, int size) {
        Pageable pageable = buildCategoryPageable(page, size,
                Sort.by(Sort.Direction.DESC, "averageRating")
                        .and(Sort.by(Sort.Direction.DESC, "reviewCount")));
        return productRepository.findByProductCategory_CategoryId(categoryId, pageable);
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

    @Transactional(readOnly = true)
    public List<ProductOption> getOptionsByProductId(Long productId) {
        return productOptionRepository.findByProduct_ProductId(productId);
    }

    public List<ProductImage> getImagesByProductId(Long productId) {
        return productImageRepository.findByProduct_ProductId(productId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAllProductsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Product> productPage = productRepository.findAll(pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("products", productPage.getContent());
        result.put("totalElements", productPage.getTotalElements());
        result.put("totalPages", productPage.getTotalPages());

        return result;
    }

    @Transactional(readOnly = true)
    public List<ProductCategory> getTopLevelCategoriesWithChildren() {
        List<ProductCategory> topCategories = productCategoryRepository.findAllByLevel(1);
        topCategories.sort((a, b) -> {
            int orderCompare = Integer.compare(
                    a.getDisplayOrder() != null ? a.getDisplayOrder() : Integer.MAX_VALUE,
                    b.getDisplayOrder() != null ? b.getDisplayOrder() : Integer.MAX_VALUE
            );
            if (orderCompare != 0) {
                return orderCompare;
            }
            return a.getCategoryName().compareToIgnoreCase(b.getCategoryName());
        });

        // Ensure child categories are loaded within the transactional context
        topCategories.forEach(category -> category.getChildCategories().size());
        return topCategories;
    }

    @Transactional(readOnly = true)
    public Optional<CategoryContext> getCategoryContext(Long categoryId) {
        return productCategoryRepository.findById(categoryId)
                .map(category -> new CategoryContext(
                        category.getCategoryId(),
                        category.getParentCategory() != null
                                ? category.getParentCategory().getCategoryId()
                                : null
                ));
    }

    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String keyword, int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            return Page.empty(PageRequest.of(Math.max(page - 1, 0), size));
        }

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        return productRepository.findProductsByProductTitleContainingIgnoreCase(keyword.trim(), pageable);
    }

    private Pageable buildCategoryPageable(int page, int size, Sort sort) {
        int pageIndex = Math.max(page - 1, 0);
        int pageSize = Math.max(size, 1);
        Sort effectiveSort;
        if (sort == null || sort.isUnsorted()) {
            effectiveSort = DEFAULT_CATEGORY_SORT;
        } else {
            effectiveSort = sort.and(DEFAULT_CATEGORY_SORT);
        }
        return PageRequest.of(pageIndex, pageSize, effectiveSort);
    }

    @Getter
    public static final class CategoryContext {
        private final Long categoryId;
        private final Long parentCategoryId;

        public CategoryContext(Long categoryId, Long parentCategoryId) {
            this.categoryId = categoryId;
            this.parentCategoryId = parentCategoryId;
        }

    }
}
