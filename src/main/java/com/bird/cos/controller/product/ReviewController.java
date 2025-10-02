package com.bird.cos.controller.product;

import com.bird.cos.domain.product.Review;
import com.bird.cos.dto.product.ReviewRequest;
import com.bird.cos.dto.product.ReviewResponse;
import com.bird.cos.dto.product.ReviewUpdateRequest;
import com.bird.cos.repository.product.ProductOptionRepository;
import com.bird.cos.repository.product.ReviewRepository;
import com.bird.cos.security.CustomUserDetails;
import com.bird.cos.service.product.ReviewService;
import com.bird.cos.service.product.ProductService;
import com.bird.cos.domain.product.ProductOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ProductService productService;
    private final ReviewRepository reviewRepository;
    private final ProductOptionRepository productOptionRepository;

    // 모든 리뷰 조회 (뷰 렌더링)
    @GetMapping("/reviews")
    public String getAllReviews(@RequestParam(required = false, defaultValue = "all") String filter,
                                @RequestParam(required = false, defaultValue = "latest") String sort,
                                @RequestParam(required = false) String ratingRange,
                                Model model) {
        try {
            List<ReviewResponse> reviews = reviewService.findAllReviewsWithFilter(filter, sort, ratingRange, null);
            Map<String, Integer> filterCounts = reviewService.getFilterCounts(null);

            model.addAttribute("reviews", reviews);
            model.addAttribute("reviewCount", reviews.size());
            model.addAttribute("currentFilter", filter);
            model.addAttribute("currentSort", sort);
            model.addAttribute("currentRatingRange", ratingRange);
            model.addAttribute("filterCounts", filterCounts);

            double averageRating = reviewService.calculateAverageRating(reviews);
            model.addAttribute("averageRating", averageRating);

            return "review/list"; // 템플릿 이름 반환
        } catch (Exception e) {
            log.error("전체 리뷰 조회 중 오류 발생", e);
            model.addAttribute("error", "리뷰를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "review/error";
        }
    }

    @GetMapping("/product/{productId}/reviews")
    public String getReviewsByProduct(@PathVariable Long productId,
                                      @RequestParam(required = false, defaultValue = "all") String filter,
                                      @RequestParam(required = false, defaultValue = "latest") String sort,
                                      @RequestParam(required = false) String ratingRange,
                                      @RequestParam(required = false) Long optionId,
                                      @RequestParam(required = false, defaultValue = "1") int page,
                                      @RequestParam(required = false, defaultValue = "10") int size,
                                      Model model,
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 페이징 적용된 필터링된 리뷰
            Map<String, Object> result = reviewService.findReviewsByProductIdWithFilterPage(
                    productId, filter, sort, ratingRange, optionId, page, size);
            List<ReviewResponse> reviews = (List<ReviewResponse>) result.get("reviews");
            int totalPages = (Integer) result.get("totalPages");
            long totalElements = (Long) result.get("totalElements");

            // 전체 리뷰 개수
            Long totalReviewCount = reviewRepository.countByProductId(productId);

            // 전체 평균 별점
            List<ReviewResponse> allProductReviews = reviewService.findReviewsByProductIdWithFilter(
                    productId, "all", "latest", null, null);
            double overallAverageRating = reviewService.calculateAverageRating(allProductReviews);

            // 필터링된 리뷰의 평균 별점
            double filteredAverageRating = reviewService.calculateAverageRating(reviews);

            // 필터 카운트
            Map<String, Integer> filterCounts = reviewService.getFilterCounts(productId);
            Map<String, Integer> ratingCounts = reviewService.getRatingCounts(productId);
            List<ProductOption> productOptions = productService.getOptionsByProductId(productId);

            // 모델에 데이터 추가
            model.addAttribute("reviews", reviews);
            model.addAttribute("productId", productId);

            // 전체 통계 (고정값)
            model.addAttribute("totalReviewCount", totalReviewCount);
            model.addAttribute("overallAverageRating", overallAverageRating);

            // 필터링된 결과
            model.addAttribute("reviewCount", totalElements);
            model.addAttribute("averageRating", filteredAverageRating);

            // 필터 상태
            model.addAttribute("currentFilter", filter);
            model.addAttribute("currentSort", sort);
            model.addAttribute("currentRatingRange", ratingRange);
            model.addAttribute("currentOptionId", optionId);

            // 페이징
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);

            // 기타 정보
            model.addAttribute("filterCounts", filterCounts);
            model.addAttribute("ratingCounts", ratingCounts);
            model.addAttribute("productOptions", productOptions);

            // 현재 로그인한 사용자 정보
            if (userDetails != null) {
                model.addAttribute("currentUserNickname", userDetails.getNickname());
            }

            return "review/list";
        } catch (Exception e) {
            log.error("상품별 리뷰 조회 중 오류 발생", e);
            model.addAttribute("error", "리뷰를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "review/list";
        }
    }

    
    // 특정 리뷰 상세 조회
    @GetMapping("/reviews/{reviewId}")
    public String getReviewDetail(@PathVariable Long reviewId, Model model) {
        try {
            ReviewResponse review = reviewService.findById(reviewId);
            model.addAttribute("review", review);
            return "review/detail";
        } catch (Exception e) {
            log.error("리뷰 상세 조회 중 오류 발생", e);
            model.addAttribute("error", "리뷰를 찾을 수 없습니다: " + e.getMessage());
            return "review/error";
        }
    }

    // 리뷰 작성 페이지 이동
    @GetMapping("/product/{productId}/reviews/new")
    public String reviewForm(@PathVariable Long productId, Model model) {
        try {
            model.addAttribute("reviewRequest", new ReviewRequest());
            model.addAttribute("productId", productId);
            model.addAttribute("isEditMode", false); // 작성 모드

            // 상품 옵션 목록 조회
            List<ProductOption> productOptions = productService.getOptionsByProductId(productId);
            model.addAttribute("productOptions", productOptions);

            return "review/form";
        } catch (Exception e) {
            log.error("리뷰 작성 페이지 로딩 중 오류 발생", e);
            model.addAttribute("error", "리뷰 작성 페이지를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "review/error";
        }
    }

    // 리뷰 생성
    @PostMapping("/product/{productId}/reviews")
    @PreAuthorize("isAuthenticated()")
    public String createReview(@PathVariable Long productId,
                               @ModelAttribute ReviewRequest requestDto,
                               @RequestParam(value = "images", required = false) List<MultipartFile> imageFiles,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        try {
            String userNickname = userDetails.getNickname();
            reviewService.createReview(productId, requestDto, userNickname, imageFiles);
            return "redirect:/product/" + productId+ "#reviews";
        } catch (Exception e) {
            log.error("리뷰 작성 중 오류 발생", e);
            model.addAttribute("error", "리뷰 작성 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("reviewRequest", requestDto);
            model.addAttribute("productId", productId);
            model.addAttribute("isEditMode", false);

            // 에러 발생 시에도 옵션 목록 다시 로드
            try {
                List<ProductOption> productOptions = productService.getOptionsByProductId(productId);
                model.addAttribute("productOptions", productOptions);
            } catch (Exception ex) {
                log.error("상품 옵션 목록 로딩 중 오류 발생", ex);
            }

            return "review/form";
        }
    }

    // 리뷰 수정 폼 이동 - URL 패턴 변경
    @GetMapping("/reviews/{reviewId}/edit")
    @PreAuthorize("isAuthenticated()")
    public String editReviewForm(@PathVariable Long reviewId,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model) {
        try {
            log.info("리뷰 수정 페이지 요청 - reviewId: {}, user: {}", reviewId, userDetails != null ? userDetails.getNickname() : "null");

            // 리뷰 존재 여부 확인
            ReviewResponse review = reviewService.findById(reviewId);
            log.info("리뷰 조회 완료 - reviewId: {}, title: {}", reviewId, review.getTitle());

            // 권한 확인
            if (!review.getUserNickname().equals(userDetails.getNickname())) {
                log.warn("리뷰 수정 권한 없음 - reviewId: {}, owner: {}, requester: {}",
                        reviewId, review.getUserNickname(), userDetails.getNickname());
                return "redirect:/access-denied";
            }

            // ReviewRequest 객체로 변환하여 폼에 바인딩
            ReviewRequest reviewRequest = new ReviewRequest();
            reviewRequest.setTitle(review.getTitle());
            reviewRequest.setContent(review.getContent());
            reviewRequest.setRating(review.getRating());
            reviewRequest.setOptionId(review.getOptionId());

            model.addAttribute("review", review); // 기존 리뷰 데이터
            model.addAttribute("reviewRequest", reviewRequest); // 폼 바인딩용
            model.addAttribute("productId", review.getProductId());
            model.addAttribute("isEditMode", true); // 수정 모드

            // 상품 옵션 목록도 함께 전달
            try {
                List<ProductOption> productOptions = productService.getOptionsByProductId(review.getProductId());
                model.addAttribute("productOptions", productOptions);
                log.info("상품 옵션 목록 로딩 완료 - productId: {}, options count: {}",
                        review.getProductId(), productOptions.size());
            } catch (Exception e) {
                log.error("상품 옵션 목록 로딩 중 오류 발생 - productId: {}", review.getProductId(), e);
                // 옵션 목록 로딩 실패해도 리뷰 수정은 가능하도록 처리
                model.addAttribute("productOptions", List.of());
            }

            log.info("리뷰 수정 페이지 모델 설정 완료 - reviewId: {}", reviewId);
            return "review/form"; // 통합된 form 템플릿 사용

        } catch (NoSuchElementException e) {
            log.error("리뷰를 찾을 수 없음 - reviewId: {}", reviewId, e);
            model.addAttribute("error", "존재하지 않는 리뷰입니다.");
            return "review/error";
        } catch (Exception e) {
            log.error("리뷰 수정 페이지 로딩 중 오류 발생 - reviewId: {}", reviewId, e);
            model.addAttribute("error", "리뷰 수정 페이지를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "review/error";
        }
    }

    // 리뷰 수정 처리
    @PostMapping("/reviews/{reviewId}/edit")
    @PreAuthorize("isAuthenticated()")
    public String updateReview(@PathVariable Long reviewId,
                               @ModelAttribute ReviewUpdateRequest updateRequest,
                               @RequestParam(value = "images", required = false) List<MultipartFile> images, // 파라미터명 수정
                               @RequestParam(value = "deletedImageIndexes", required = false) List<Integer> deletedImageIndexes, // 추가!
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        String userNickname = userDetails.getNickname();
        try {
            log.info("리뷰 수정 요청 - reviewId: {}, deletedImageIndexes: {}", reviewId, deletedImageIndexes);

            // deletedImageIndexes를 updateRequest에 설정
            if (deletedImageIndexes != null && !deletedImageIndexes.isEmpty()) {
                updateRequest.setDeletedImageIndexes(deletedImageIndexes);
            }

            // 새 이미지 파일도 설정
            if (images != null && !images.isEmpty()) {
                updateRequest.setNewImages(images);
            }

            ReviewResponse updatedReview = reviewService.updateReview(reviewId, updateRequest, userNickname);

            redirectAttributes.addFlashAttribute("message", "리뷰가 성공적으로 수정되었습니다.");
            return "redirect:/product/" + updatedReview.getProductId() + "#reviews";
        } catch (AccessDeniedException e) {
            log.error("리뷰 수정 권한 없음 - reviewId: {}, user: {}", reviewId, userNickname, e);
            redirectAttributes.addFlashAttribute("error", "수정 권한이 없습니다.");
            return "redirect:/access-denied";
        } catch (Exception e) {
            log.error("리뷰 수정 중 오류 발생 - reviewId: {}, user: {}", reviewId, userNickname, e);
            redirectAttributes.addFlashAttribute("error", "리뷰 수정 중 오류가 발생했습니다: " + e.getMessage());

            redirectAttributes.addFlashAttribute("reviewUpdateRequest", updateRequest);
            return "redirect:/reviews/" + reviewId + "/edit";
        }
    }

    // 리뷰 삭제도 동일하게 변경
    @PostMapping("/reviews/{reviewId}/delete")
    @PreAuthorize("isAuthenticated()")
    public String deleteReview(@PathVariable Long reviewId,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               @RequestParam(required = false) Long productId) {
        try {
            // 리뷰 정보를 먼저 가져와서 productId를 얻음
            if (productId == null) {
                ReviewResponse review = reviewService.findById(reviewId);
                productId = review.getProductId();
            }

            reviewService.deleteReview(reviewId, userDetails.getNickname());
            return "redirect:/product/" + productId + "#reviews";
        } catch (Exception e) {
            log.error("리뷰 삭제 중 오류 발생 - reviewId: {}", reviewId, e);
            return "redirect:/reviews?error=" + e.getMessage();
        }
    }

    // AJAX용 리뷰 수정 API
    @PostMapping(value = "/api/reviews/{reviewId}", consumes = {"multipart/form-data"}) // 1. consumes 타입 지정
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateReviewApi(
            @PathVariable Long reviewId,
            @ModelAttribute ReviewUpdateRequest updateRequest, // 2. @RequestBody -> @RequestPart
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages, // 3. 파일 파라미터 추가
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 4. 서비스 호출 시 newImages 추가
            ReviewResponse updatedReview = reviewService.updateReview(reviewId, updateRequest, userDetails.getNickname());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "review", updatedReview,
                    "message", "리뷰가 성공적으로 수정되었습니다."
            ));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("success", false, "error", "수정 권한이 없습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // AJAX용 리뷰 삭제 API
    @DeleteMapping("/api/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteReviewApi(@PathVariable Long reviewId,
                                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            reviewService.deleteReview(reviewId, userDetails.getNickname());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "리뷰가 성공적으로 삭제되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    // AJAX용 API
    @GetMapping("/api/product/{productId}/reviews")
    @ResponseBody
    public Map<String, Object> getReviewsApi(@PathVariable Long productId,
                                             @RequestParam(required = false, defaultValue = "all") String filter,
                                             @RequestParam(required = false, defaultValue = "latest") String sort,
                                             @RequestParam(required = false) String ratingRange,
                                             @RequestParam(required = false) Long optionId) {
        try {
            List<ReviewResponse> reviews = reviewService.findReviewsByProductIdWithFilter(
                    productId, filter, sort, ratingRange, optionId);
            Map<String, Integer> filterCounts = reviewService.getFilterCounts(productId);
            Map<String, Integer> ratingCounts = reviewService.getRatingCounts(productId);
            double averageRating = reviewService.calculateAverageRating(reviews);

            return Map.of(
                    "reviews", reviews,
                    "reviewCount", reviews.size(),
                    "filterCounts", filterCounts,
                    "ratingCounts", ratingCounts,
                    "averageRating", averageRating,
                    "success", true
            );
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "error", e.getMessage()
            );
        }
    }
}