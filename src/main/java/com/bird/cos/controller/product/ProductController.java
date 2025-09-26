package com.bird.cos.controller.product;

import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.product.ProductOption;
import com.bird.cos.dto.product.ReviewResponse;
import com.bird.cos.service.product.ProductService;
import com.bird.cos.service.product.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ProductController {
    @Autowired
    ProductService productService;

    @Autowired
    ReviewService reviewService;

    //카테고리별 조회
    @GetMapping("/product/category/{categoryId}")
    public String showProductsByCategory(Model model, @PathVariable Long categoryId) {

        List<Product> products = productService.getProductsByCategory(categoryId);

        model.addAttribute("products", products);
        model.addAttribute("totalCount", products != null ? products.size() : 0);
        return "product/productList";
    }

    //특정 카테고리의 상품을 세일가격 기준 오름차순으로 조회
    @GetMapping("/product/category/{categoryId}/price-asc")
    public String showProductsByCategoryOrderByPriceAsc(Model model, @PathVariable Long categoryId) {
        List<Product> products = productService.getProductsByCategoryOrderBySalePriceAsc(categoryId);
        model.addAttribute("products", products);
        model.addAttribute("totalCount", products != null ? products.size() : 0);
        return "product/productList";
    }

    //특정 카테고리의 상품을 세일가격 기준 내림차순으로 조회
    @GetMapping("/product/category/{categoryId}/price-desc")
    public String showProductsByCategoryOrderByPriceDesc(Model model, @PathVariable Long categoryId) {
        List<Product> products = productService.getProductsByCategoryOrderBySalePriceDesc(categoryId);
        model.addAttribute("products", products);
        model.addAttribute("totalCount", products != null ? products.size() : 0);
        return "product/productList";
    }

    // 특정 카테고리의 상품을 평균평점 기준 내림차순으로 조회 (평점 높은순)
    @GetMapping("/product/category/{categoryId}/rating-desc")
    public String showProductsByCategoryOrderByRating(Model model, @PathVariable Long categoryId) {
        // Service에 정의된, 평점순으로 조회하는 메서드를 호출합니다.
        List<Product> products = productService.getProductsByCategoryOrderByRatingDesc(categoryId);
        model.addAttribute("products", products);
        model.addAttribute("totalCount", products != null ? products.size() : 0);
        return "product/productList";
    }

    //상품 페이지
    @GetMapping("/product")
    public String selectProduct(Model model) {
        //실제 데이터를 받아오는 기능
        List <Product> products = productService.getAllProducts();
        model.addAttribute("products", products);

        //실제 데이터 카운트하는 기능
        int totalCount = products.size();
        model.addAttribute("totalCount", totalCount);

        return "product/product";
    }

    //상세페이지 (수정된 메소드)
    @GetMapping("/product/{productId}")
    public String productDetail(@PathVariable Long productId,
                                // 리뷰 필터링을 위한 파라미터 추가
                                @RequestParam(required = false, defaultValue = "all") String filter,
                                @RequestParam(required = false, defaultValue = "latest") String sort,
                                @RequestParam(required = false) String ratingRange,
                                @RequestParam(required = false) Long optionId,
                                @RequestParam(required = false, defaultValue = "1") int page,
                                @RequestParam(required = false, defaultValue = "10") int size,
                                Model model) {
        Optional<Product> productOpt = productService.getProductById(productId);

        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            model.addAttribute("product", product);
            model.addAttribute("options", product.getOptions());
            model.addAttribute("brandId", product.getBrand().getBrandId());
            model.addAttribute("brand", product.getBrand());
            model.addAttribute("basePrice", product.getSalePrice());

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

            return "product/productDetail";
        } else {
            return "redirect:/";
        }
    }
}