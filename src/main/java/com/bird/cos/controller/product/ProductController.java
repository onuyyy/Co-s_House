package com.bird.cos.controller.product;

import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.product.ProductCategory;
import com.bird.cos.domain.product.ProductImage;
import com.bird.cos.domain.product.ProductOption;
import com.bird.cos.dto.product.ReviewResponse;
import com.bird.cos.security.CustomUserDetails;
import com.bird.cos.service.product.ProductService;
import com.bird.cos.service.product.ReviewService;
import com.bird.cos.service.question.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class ProductController {
    @Autowired
    ProductService productService;

    @Autowired
    ReviewService reviewService;

    @Autowired
    QuestionService questionService;

    @ModelAttribute("productNavCategories")
    public List<CategoryNavGroup> productNavCategories() {
        return productService.getTopLevelCategoriesWithChildren().stream()
                .map(ProductController::toNavGroup)
                .collect(Collectors.toList());
    }

    //카테고리별 조회
    @GetMapping("/product/category/{categoryId}")
    public String showProductsByCategory(Model model,
                                        @PathVariable Long categoryId,
                                        @RequestParam(required = false, defaultValue = "1") int page,
                                        @RequestParam(required = false, defaultValue = "12") int size) {

        int pageSize = Math.max(size, 1);
        int pageNumber = Math.max(page, 1);
        Page<Product> products = productService.getProductsByCategory(categoryId, pageNumber, pageSize);
        populateCategoryListing(model, categoryId, products, pageSize);
        return "product/productList";
    }

    //특정 카테고리의 상품을 세일가격 기준 오름차순으로 조회
    @GetMapping("/product/category/{categoryId}/price-asc")
    public String showProductsByCategoryOrderByPriceAsc(Model model,
                                                       @PathVariable Long categoryId,
                                                       @RequestParam(required = false, defaultValue = "1") int page,
                                                       @RequestParam(required = false, defaultValue = "12") int size) {
        int pageSize = Math.max(size, 1);
        int pageNumber = Math.max(page, 1);
        Page<Product> products = productService.getProductsByCategoryOrderBySalePriceAsc(categoryId, pageNumber, pageSize);
        populateCategoryListing(model, categoryId, products, pageSize);
        return "product/productList";
    }

    //특정 카테고리의 상품을 세일가격 기준 내림차순으로 조회
    @GetMapping("/product/category/{categoryId}/price-desc")
    public String showProductsByCategoryOrderByPriceDesc(Model model,
                                                        @PathVariable Long categoryId,
                                                        @RequestParam(required = false, defaultValue = "1") int page,
                                                        @RequestParam(required = false, defaultValue = "12") int size) {
        int pageSize = Math.max(size, 1);
        int pageNumber = Math.max(page, 1);
        Page<Product> products = productService.getProductsByCategoryOrderBySalePriceDesc(categoryId, pageNumber, pageSize);
        populateCategoryListing(model, categoryId, products, pageSize);
        return "product/productList";
    }

    // 특정 카테고리의 상품을 평균평점 기준 내림차순으로 조회 (평점 높은순)
    @GetMapping("/product/category/{categoryId}/rating-desc")
    public String showProductsByCategoryOrderByRating(Model model,
                                                     @PathVariable Long categoryId,
                                                     @RequestParam(required = false, defaultValue = "1") int page,
                                                     @RequestParam(required = false, defaultValue = "12") int size) {
        int pageSize = Math.max(size, 1);
        int pageNumber = Math.max(page, 1);
        Page<Product> products = productService.getProductsByCategoryOrderByRatingDesc(categoryId, pageNumber, pageSize);
        populateCategoryListing(model, categoryId, products, pageSize);
        return "product/productList";
    }

    //상품 페이지
    @GetMapping("/product")
    public String selectProduct(Model model,
                                @RequestParam(required = false, defaultValue = "1") int page,
                                @RequestParam(required = false, defaultValue = "12") int size) {
        // 페이징된 데이터 가져오기
        int pageSize = Math.max(size, 1);
        int pageNumber = Math.max(page, 1);
        Map<String, Object> result = productService.getAllProductsPaged(pageNumber, pageSize);

        model.addAttribute("products", result.get("products"));
        model.addAttribute("totalCount", result.get("totalElements"));
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPages", result.get("totalPages"));
        model.addAttribute("activeCategoryId", null);
        model.addAttribute("activeParentCategoryId", null);
        model.addAttribute("pageSize", pageSize);

        return "product/product";
    }

    //상세페이지
    @GetMapping("/product/{productId}")
    public String productDetail(@PathVariable Long productId,
                                // 리뷰 필터링을 위한 파라미터 추가
                                @RequestParam(required = false, defaultValue = "all") String filter,
                                @RequestParam(required = false, defaultValue = "latest") String sort,
                                @RequestParam(required = false) String ratingRange,
                                @RequestParam(required = false) Long optionId,
                                @RequestParam(required = false, defaultValue = "1") int page,
                                @RequestParam(required = false, defaultValue = "10") int size,
                                // 문의글 페이징을 위한 파라미터 추가 (리뷰와 겹치지 않게 q_ prefix 사용)
                                @RequestParam(required = false, defaultValue = "1") int q_page,
                                @RequestParam(required = false, defaultValue = "5") int q_size,
                                Model model,
                                @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Product> productOpt = productService.getProductById(productId);

        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            model.addAttribute("product", product);
            model.addAttribute("options", product.getOptions());
            model.addAttribute("brandId", product.getBrand().getBrandId());
            model.addAttribute("brand", product.getBrand());
            model.addAttribute("basePrice", product.getSalePrice());

            List<ProductImage> productImages = productService.getImagesByProductId(productId);
            model.addAttribute("productImages", productImages);
            // --- 리뷰 정보 로딩  ---
            try {
                // 1. 리뷰 목록과 페이징 정보 가져오기
                Map<String, Object> reviewResult = reviewService.findReviewsByProductIdWithFilterPage(
                        productId, filter, sort, ratingRange, optionId, page, size);
                List<ReviewResponse> reviews = (List<ReviewResponse>) reviewResult.get("reviews");
                long totalReviewCount = (Long) reviewResult.get("totalElements");

                // 2. 리뷰 통계 정보 (필터, 별점 카운트 등) 가져오기
                Map<String, Integer> filterCounts = reviewService.getFilterCounts(productId);
                Map<String, Integer> ratingCounts = reviewService.getRatingCounts(productId);
                List<ProductOption> productOptions = productService.getOptionsByProductId(productId);
                double overallAverageRating = reviewService.calculateOverallAverageRating(productId); // 전체 평균 평점

                // 3. 모델에 리뷰 관련 데이터 모두 추가
                model.addAttribute("reviews", reviews);
                model.addAttribute("totalReviewCount", totalReviewCount);
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", (Integer) reviewResult.get("totalPages"));
                model.addAttribute("productId", productId); // 리뷰 템플릿에서 사용할 productId

                // 필터링 유지를 위한 정보
                model.addAttribute("currentFilter", filter);
                model.addAttribute("currentSort", sort);
                model.addAttribute("currentRatingRange", ratingRange);
                model.addAttribute("currentOptionId", optionId);

                // 통계 정보
                model.addAttribute("filterCounts", filterCounts);
                model.addAttribute("ratingCounts", ratingCounts);
                model.addAttribute("productOptions", productOptions);
                model.addAttribute("overallAverageRating", overallAverageRating);

            } catch (Exception e) {
                // 리뷰를 불러오다 에러가 발생해도 상품 상세 페이지는 보여주도록 처리
                model.addAttribute("reviewError", "리뷰를 불러오는 중 오류가 발생했습니다.");
            }
            try {
                Map<String, Object> questionResult = questionService.findQuestionsByProductIdPaged(productId, q_page, q_size);
                model.addAttribute("questions", questionResult.get("questions"));
                model.addAttribute("questionTotalPages", questionResult.get("totalPages"));
                model.addAttribute("currentQuestionPage", q_page);
                model.addAttribute("totalQuestionCount", questionResult.get("totalElements"));
            } catch (Exception e) {
                model.addAttribute("questionError", "문의를 불러오는 중 오류가 발생했습니다.");
            }
            if (userDetails instanceof CustomUserDetails customUserDetails) {
                model.addAttribute("currentUserNickname", customUserDetails.getNickname());
            }
            return "product/productDetail";
        } else {
            return "redirect:/";
        }
    }
    @GetMapping("/admin/update-review-stats")
    public String updateAllReviewStats() {
        List<Product> products = productService.getAllProducts(); // 또는 productRepository.findAll()

        for (Product product : products) {
            try {
                reviewService.updateProductReviewStats(product.getProductId());
            } catch (Exception e) {
                // 에러가 있어도 계속 진행
                System.err.println("Failed to update stats for product: " + product.getProductId());
            }
        }

        return "redirect:/product";
    }
    private void registerActiveCategories(Model model, Long categoryId) {
        productService.getCategoryContext(categoryId)
                .ifPresentOrElse(context -> {
                    model.addAttribute("activeCategoryId", context.getCategoryId());
                    Long parentId = context.getParentCategoryId() != null ? context.getParentCategoryId() : context.getCategoryId();
                    model.addAttribute("activeParentCategoryId", parentId);
                }, () -> {
                    model.addAttribute("activeCategoryId", categoryId);
                    model.addAttribute("activeParentCategoryId", categoryId);
                });
    }

    private void populateCategoryListing(Model model, Long categoryId, Page<Product> productPage, int size) {
        model.addAttribute("products", productPage.getContent());
        long totalCount = productPage.getTotalElements();
        int totalPages = productPage.getTotalPages();
        int currentPageNumber = productPage.getNumber() + 1;
        if (totalPages == 0) {
            currentPageNumber = 1;
        } else if (currentPageNumber > totalPages) {
            currentPageNumber = totalPages;
        }

        model.addAttribute("totalCount", totalCount);
        model.addAttribute("currentPage", currentPageNumber);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", Math.max(size, 1));
        registerActiveCategories(model, categoryId);
    }

    private static CategoryNavGroup toNavGroup(ProductCategory category) {
        List<CategoryNavItem> children = category.getChildCategories().stream()
                .sorted(CATEGORY_COMPARATOR)
                .map(child -> new CategoryNavItem(child.getCategoryId(), child.getCategoryName()))
                .collect(Collectors.toList());
        return new CategoryNavGroup(category.getCategoryId(), category.getCategoryName(), children);
    }

    private static final Comparator<ProductCategory> CATEGORY_COMPARATOR = Comparator
            .comparing((ProductCategory cat) -> cat.getDisplayOrder() != null ? cat.getDisplayOrder() : Integer.MAX_VALUE)
            .thenComparing(cat -> cat.getCategoryName().toLowerCase());

    private static final class CategoryNavGroup {
        private final Long id;
        private final String label;
        private final List<CategoryNavItem> children;

        private CategoryNavGroup(Long id, String label, List<CategoryNavItem> children) {
            this.id = id;
            this.label = label;
            this.children = children;
        }

        public Long getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public List<CategoryNavItem> getChildren() {
            return children;
        }

        public boolean hasChildren() {
            return children != null && !children.isEmpty();
        }
    }

    private static final class CategoryNavItem {
        private final Long id;
        private final String label;

        private CategoryNavItem(Long id, String label) {
            this.id = id;
            this.label = label;
        }

        public Long getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }
    }
}
