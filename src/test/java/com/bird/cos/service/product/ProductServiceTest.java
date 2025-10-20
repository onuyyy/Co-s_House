package com.bird.cos.service.product;

import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.product.ProductCategory;
import com.bird.cos.domain.product.ProductImage;
import com.bird.cos.domain.product.ProductOption;
import com.bird.cos.repository.product.ProductCategoryRepository;
import com.bird.cos.repository.product.ProductImageRepository;
import com.bird.cos.repository.product.ProductOptionRepository;
import com.bird.cos.repository.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 상품 페이지 조회 로직을 담당하는 ProductService 핵심 흐름을 검증한다.
 * 카테고리/검색/페이지네이션/정렬 옵션이 올바르게 Repository로 전달되는지 집중적으로 확인한다.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @InjectMocks
    private ProductService productService;

    private Page<Product> emptyPage;

    @BeforeEach
    void setUp() {
        emptyPage = new PageImpl<>(List.of());
    }

    @Test
    void getProductsByCategory_UsesDefaultSortAndPaging() {
        // 카테고리 상품 조회 시 페이지/사이즈 정규화와 기본 정렬이 적용되는지 확인
        Long categoryId = 5L;
        when(productRepository.findByProductCategory_CategoryId(eq(categoryId), any(Pageable.class)))
                .thenReturn(emptyPage);

        productService.getProductsByCategory(categoryId, 2, 20);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findByProductCategory_CategoryId(eq(categoryId), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1); // page - 1
        assertThat(pageable.getPageSize()).isEqualTo(20);

        List<Sort.Order> orders = new ArrayList<>();
        pageable.getSort().forEach(orders::add);
        assertThat(orders).containsExactly(
                new Sort.Order(Sort.Direction.DESC, "productCreatedAt"),
                new Sort.Order(Sort.Direction.DESC, "productId")
        );
    }

    @Test
    void getProductsByCategoryOrderBySalePriceAsc_AppliesSalePriceThenDefaultSort() {
        // 세일가 오름차순 정렬 옵션이 기본 정렬과 함께 전달되는지 확인
        Long categoryId = 7L;
        when(productRepository.findByProductCategory_CategoryId(eq(categoryId), any(Pageable.class)))
                .thenReturn(emptyPage);

        productService.getProductsByCategoryOrderBySalePriceAsc(categoryId, 1, 12);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findByProductCategory_CategoryId(eq(categoryId), pageableCaptor.capture());
        List<Sort.Order> orders = new ArrayList<>();
        pageableCaptor.getValue().getSort().forEach(orders::add);

        assertThat(orders).containsExactly(
                new Sort.Order(Sort.Direction.ASC, "salePrice"),
                new Sort.Order(Sort.Direction.DESC, "productCreatedAt"),
                new Sort.Order(Sort.Direction.DESC, "productId")
        );
    }

    @Test
    void getTopLevelCategoriesWithChildren_SortsByDisplayOrderThenName() {
        // 상위 카테고리 목록을 전시 순서와 이름 기준으로 정렬하며 자식 로딩을 트리거하는지 확인
        ProductCategory catA = mock(ProductCategory.class);
        when(catA.getDisplayOrder()).thenReturn(2);
        when(catA.getCategoryName()).thenReturn("Alpha");
        when(catA.getChildCategories()).thenReturn(List.of(mock(ProductCategory.class)));

        ProductCategory catB = mock(ProductCategory.class);
        when(catB.getDisplayOrder()).thenReturn(1);
        when(catB.getCategoryName()).thenReturn("Beta");
        when(catB.getChildCategories()).thenReturn(List.of());

        ProductCategory catC = mock(ProductCategory.class);
        when(catC.getDisplayOrder()).thenReturn(2);
        when(catC.getCategoryName()).thenReturn("Charlie");
        when(catC.getChildCategories()).thenReturn(List.of());

        when(productCategoryRepository.findAllByLevel(1)).thenReturn(List.of(catA, catB, catC));

        List<ProductCategory> result = productService.getTopLevelCategoriesWithChildren();

        assertThat(result).containsExactly(catB, catA, catC);
        verify(catA, atLeastOnce()).getChildCategories();
        verify(catB, atLeastOnce()).getChildCategories();
        verify(catC, atLeastOnce()).getChildCategories();
    }

    @Test
    void searchProducts_WithBlankKeyword_ReturnsEmptyPageWithoutRepositoryCall() {
        // 검색어가 공백이면 쿼리를 실행하지 않고 빈 페이지를 돌려줘야 한다
        Page<Product> result = productService.searchProducts("   ", 3, 10);

        assertThat(result.getTotalElements()).isZero();
        verify(productRepository, never()).findProductsByProductTitleContainingIgnoreCase(any(), any());
    }

    @Test
    void searchProducts_WithKeyword_DelegatesToRepository() {
        // 검색어가 있으면 공백 제거 후 Repository에 전달한다
        when(productRepository.findProductsByProductTitleContainingIgnoreCase(eq("table"), any(Pageable.class)))
                .thenReturn(emptyPage);

        productService.searchProducts("  table  ", 2, 15);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findProductsByProductTitleContainingIgnoreCase(eq("table"), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(15);
    }

    @Test
    void getAllProductsPaged_ReturnsPagingMetadata() {
        // 전체 상품 페이지 조회 시 결과 맵에 콘텐츠와 메타 정보가 담기는지 확인
        Product product = mock(Product.class);
        Page<Product> page = new PageImpl<>(List.of(product), Pageable.ofSize(1), 5);
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        Map<String, Object> result = productService.getAllProductsPaged(1, 1);

        assertThat(result.get("products")).isEqualTo(List.of(product));
        assertThat(result.get("totalElements")).isEqualTo(5L);
        assertThat(result.get("totalPages")).isEqualTo(5);
        verify(productRepository).findAll(any(Pageable.class));
    }

    @Test
    void getCategoryContext_ReturnsParentInfo() {
        // 카테고리 컨텍스트 조회 시 부모 ID까지 포함해 내려주는지 확인
        ProductCategory category = mock(ProductCategory.class);
        ProductCategory parent = mock(ProductCategory.class);
        when(category.getCategoryId()).thenReturn(10L);
        when(parent.getCategoryId()).thenReturn(1L);
        when(category.getParentCategory()).thenReturn(parent);
        when(productCategoryRepository.findById(10L)).thenReturn(Optional.of(category));

        Optional<ProductService.CategoryContext> result = productService.getCategoryContext(10L);

        assertThat(result).isPresent();
        assertThat(result.get().getCategoryId()).isEqualTo(10L);
        assertThat(result.get().getParentCategoryId()).isEqualTo(1L);
    }

    @Test
    void getOptionsAndImages_DelegatesToRepositories() {
        // 옵션/이미지 조회가 각 레포지토리에 위임되는지 확인
        Long productId = 55L;
        List<ProductOption> options = List.of(mock(ProductOption.class));
        List<ProductImage> images = List.of(mock(ProductImage.class));
        when(productOptionRepository.findByProduct_ProductId(productId)).thenReturn(options);
        when(productImageRepository.findByProduct_ProductId(productId)).thenReturn(images);

        assertThat(productService.getOptionsByProductId(productId)).isEqualTo(options);
        assertThat(productService.getImagesByProductId(productId)).isEqualTo(images);
    }
}
