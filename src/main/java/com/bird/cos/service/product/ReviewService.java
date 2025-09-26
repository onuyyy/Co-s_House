package com.bird.cos.service.product;

import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.product.ProductOption;
import com.bird.cos.domain.product.Review;
import com.bird.cos.domain.product.ReviewImage; // ReviewImage import
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.product.ReviewRequest;
import com.bird.cos.dto.product.ReviewResponse;
import com.bird.cos.dto.product.ReviewUpdateRequest; // ReviewUpdateRequest import
import com.bird.cos.repository.product.ProductOptionRepository;
import com.bird.cos.repository.product.ProductRepository;
import com.bird.cos.repository.product.ReviewImageRepository; // ReviewImageRepository import
import com.bird.cos.repository.product.ReviewRepository;
import com.bird.cos.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Slf4j import
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException; // Spring Security의 AccessDeniedException 사용
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.jpa.domain.Specification;
import com.bird.cos.repository.product.ReviewSpecificationRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용 트랜잭션 (기본)
@Slf4j // 로그 사용을 위해 추가
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ReviewImageService reviewImageService;
    private final ReviewImageRepository reviewImageRepository;

    // ID로 리뷰 조회
    public ReviewResponse findById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 리뷰입니다: " + reviewId));
        return ReviewResponse.fromEntity(review);
    }


    private void checkReviewPermission(Long reviewId, String userNickname) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 리뷰입니다: " + reviewId));
        if (!review.getUser().getUserNickname().equals(userNickname)) {
            throw new AccessDeniedException("리뷰에 대한 권한이 없습니다.");
        }
    }

    // 모든 리뷰 조회 (필터링 포함)
    public List<ReviewResponse> findAllReviewsWithFilter(String filter, String sort, String ratingRange, Long productId) {
        List<Review> reviews;

        if (productId != null) {
            // N+1 문제 방지를 위해 fetch join 사용 고려
            reviews = reviewRepository.findByProduct_ProductId(productId);
        } else {
            // N+1 문제 방지를 위해 fetch join 사용 고려 (ex: findAllWithUserAndProductAndImages())
            reviews = reviewRepository.findAll();
        }

        reviews = applyFilters(reviews, filter, ratingRange); // 필터링 로직
        reviews = applySorting(reviews, sort); // 정렬 로직

        return reviews.stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> findReviewsByProductIdWithFilterPage(Long productId, String filter, String sort,
                                                                    String ratingRange, Long optionId, int page, int size) {
        // 1. 정렬 조건 생성 (기존과 동일)
        Pageable pageable = createPageable(page - 1, size, sort);

        // 2. 동적 쿼리 조건(Specification) 생성
        Specification<Review> spec = Specification.where(ReviewSpecificationRepository.hasProductId(productId));

        if (optionId != null) {
            spec = spec.and(ReviewSpecificationRepository.hasOptionId(optionId));
        }
        if (ratingRange != null && !ratingRange.isEmpty()) {
            spec = spec.and(ReviewSpecificationRepository.inRatingRange(ratingRange));
        }

        if ("photo".equals(filter)) {
            spec = spec.and(ReviewSpecificationRepository.isPhotoReview());
        } else if ("verified".equals(filter)) {
            spec = spec.and(ReviewSpecificationRepository.isVerifiedPurchase());
        }

        // 3. Specification을 사용하여 데이터 조회
        Page<Review> reviewPage = reviewRepository.findAll(spec, pageable);

        // 4. DTO로 변환하여 결과 반환
        List<ReviewResponse> reviewResponses = reviewPage.getContent().stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("reviews", reviewResponses);
        result.put("totalPages", reviewPage.getTotalPages());
        result.put("totalElements", reviewPage.getTotalElements());
        result.put("currentPage", page);
        result.put("hasNext", reviewPage.hasNext());
        result.put("hasPrevious", reviewPage.hasPrevious());

        return result;
    }

    // 상품별 리뷰 조회 (페이징 없음)
    @Transactional(readOnly = true) // readOnly 명시
    public List<ReviewResponse> findReviewsByProductIdWithFilter(Long productId, String filter, String sort,
                                                                 String ratingRange, Long optionId) {

        int largePageSize = 10000;
        Pageable pageable = createPageable(0, largePageSize, sort);

        Page<Review> reviewPage;
        if (optionId != null) {
            reviewPage = reviewRepository.findByProduct_ProductIdAndProductOption_OptionId(productId, optionId, pageable);
        } else {
            reviewPage = reviewRepository.findByProduct_ProductId(productId, pageable);
        }

        List<Review> reviews = reviewPage.getContent();
        reviews = applyFilters(reviews, filter, ratingRange);

        return reviews.stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }
  
    private List<Review> applyRatingFilter(List<Review> reviews, String ratingRange) {
        if (ratingRange == null || ratingRange.isEmpty()) {
            return reviews;
        }

        switch (ratingRange) {
            case "5":
                return reviews.stream()
                        .filter(r -> r.getRating() != null && r.getRating().compareTo(BigDecimal.valueOf(5)) == 0)
                        .collect(Collectors.toList());
            case "4":
                return reviews.stream()
                        .filter(r -> r.getRating() != null && r.getRating().compareTo(BigDecimal.valueOf(4)) == 0)
                        .collect(Collectors.toList());
            case "3":
                return reviews.stream()
                        .filter(r -> r.getRating() != null && r.getRating().compareTo(BigDecimal.valueOf(3)) == 0)
                        .collect(Collectors.toList());
            case "2":
                return reviews.stream()
                        .filter(r -> r.getRating() != null && r.getRating().compareTo(BigDecimal.valueOf(2)) == 0)
                        .collect(Collectors.toList());
            case "1":
                return reviews.stream()
                        .filter(r -> r.getRating() != null && r.getRating().compareTo(BigDecimal.valueOf(1)) == 0)
                        .collect(Collectors.toList());
            case "4-5":
                return reviews.stream()
                        .filter(r -> r.getRating() != null
                                && r.getRating().compareTo(BigDecimal.valueOf(4)) >= 0
                                && r.getRating().compareTo(BigDecimal.valueOf(5)) <= 0)
                        .collect(Collectors.toList());
            case "3-5":
                return reviews.stream()
                        .filter(r -> r.getRating() != null
                                && r.getRating().compareTo(BigDecimal.valueOf(3)) >= 0
                                && r.getRating().compareTo(BigDecimal.valueOf(5)) <= 0)
                        .collect(Collectors.toList());
            case "1-2":
                return reviews.stream()
                        .filter(r -> r.getRating() != null
                                && r.getRating().compareTo(BigDecimal.valueOf(1)) >= 0
                                && r.getRating().compareTo(BigDecimal.valueOf(2)) <= 0)
                        .collect(Collectors.toList());
            default:
                return reviews;
        }
    }

    // 리뷰 생성 (Transactional 추가)
    @Transactional // 쓰기 작업이므로 @Transactional 필요
    public ReviewResponse createReview(Long productId, ReviewRequest requestDto, String userNickname, List<MultipartFile> imageFiles) throws IOException {
        User user = userRepository.findByUserNickname(userNickname)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + userNickname));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + productId));

        ProductOption productOption = null;
        if (requestDto.getOptionId() != null && requestDto.getOptionId() > 0) {
            productOption = productOptionRepository.findById(requestDto.getOptionId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 옵션입니다: " + requestDto.getOptionId()));
        }

        boolean isPhotoReview = (imageFiles != null && !imageFiles.stream().allMatch(MultipartFile::isEmpty));

        Review review = Review.builder()
                .product(product)
                .user(user)
                .productOption(productOption)
                .title(requestDto.getTitle())
                .rating(requestDto.getRating())
                .reviewContent(requestDto.getContent())
                .isVerifiedPurchase(true) // 기본값으로 설정?
                .isPhotoReview(isPhotoReview)
                .build();

        Review savedReview = reviewRepository.save(review); // 리뷰 저장

        if (isPhotoReview) {
            List<String> storedFileNames = reviewImageService.storeFiles(imageFiles);
            for (String storedFileName : storedFileNames) {
                ReviewImage reviewImage = new ReviewImage(storedFileName, savedReview);
                // Review 엔티티에 `cascade = CascadeType.ALL`이 설정되어 있다면
                // `savedReview.addReviewImage(reviewImage);`만으로도 저장됩니다.
                // 명시적 저장을 원한다면 `reviewImageRepository.save(reviewImage);`를 사용.
                savedReview.addReviewImage(reviewImage); // 양방향 관계 설정
            }
        }


        Review fullyLoadedReview = reviewRepository.findById(savedReview.getReviewId())
                .orElseThrow(() -> new IllegalStateException("리뷰를 다시 불러올 수 없습니다."));

        return ReviewResponse.fromEntity(fullyLoadedReview);
    }

    // 리뷰 수정
    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewUpdateRequest updateRequest, String userNickname) throws IOException {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 리뷰입니다: " + reviewId));

        // 권한 확인
        if (!review.getUser().getUserNickname().equals(userNickname)) {
            throw new AccessDeniedException("리뷰를 수정할 권한이 없습니다.");
        }

        // 1. 리뷰 내용 업데이트
        // 옵션 ID를 통해 ProductOption 엔티티를 찾아서 설정
        ProductOption productOption = null;
        if (updateRequest.getOptionId() != null && updateRequest.getOptionId() > 0) {
            productOption = productOptionRepository.findById(updateRequest.getOptionId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 옵션입니다: " + updateRequest.getOptionId()));
        }

        BigDecimal rating = updateRequest.getRating() != null
                ? BigDecimal.valueOf(updateRequest.getRating())
                : null;

        review.updateReviewContent(
                updateRequest.getTitle(),
                updateRequest.getContent(),
                rating,
                productOption
        );

        // 2. 이미지 처리
        // 2-1. 삭제할 기존 이미지 처리
        if (updateRequest.getDeletedImageIds() != null && !updateRequest.getDeletedImageIds().isEmpty()) {
            for (Long imageId : updateRequest.getDeletedImageIds()) {
                reviewImageRepository.findById(imageId).ifPresent(reviewImage -> {
                    reviewImageService.deleteFile(reviewImage.getStoredFileName()); // 물리적 파일 삭제
                    reviewImageRepository.delete(reviewImage); // DB 엔티티 삭제
                    review.getReviewImages().remove(reviewImage); // Review 엔티티 컬렉션에서도 제거
                });
            }
        }

        // 2-2. 새로 업로드할 이미지 처리
        boolean hasNewImageFiles = (updateRequest.getNewImageFiles() != null &&
                !updateRequest.getNewImageFiles().stream().allMatch(MultipartFile::isEmpty));

        if (hasNewImageFiles) {
            List<MultipartFile> actualNewFiles = updateRequest.getNewImageFiles().stream()
                    .filter(f -> !f.isEmpty())
                    .collect(Collectors.toList());
            List<String> newStoredFileNames = reviewImageService.storeFiles(actualNewFiles);
            for (String storedFileName : newStoredFileNames) {
                ReviewImage newReviewImage = new ReviewImage(storedFileName, review);
                review.addReviewImage(newReviewImage); // Review 엔티티 컬렉션에 추가
                reviewImageRepository.save(newReviewImage); // 명시적 저장 (cascade 없어도 됨)
            }
        }

        // 2-3. isPhotoReview 상태 업데이트
        // 현재 남아있는 이미지가 하나라도 있으면 isPhotoReview = true
        // 남아있는 이미지가 없으면 isPhotoReview = false
        review.setIsPhotoReview(!review.getReviewImages().isEmpty());

        Review updatedReview = reviewRepository.save(review);

        Review fullyLoadedReview = reviewRepository.findById(updatedReview.getReviewId())
                .orElseThrow(() -> new IllegalStateException("리뷰를 다시 불러올 수 없습니다."));

        return ReviewResponse.fromEntity(fullyLoadedReview);
    }

    // 리뷰 삭제
    @Transactional // 쓰기 작업이므로 @Transactional 필요
    public void deleteReview(Long reviewId, String userNickname) { // userNickname으로 변경
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 리뷰입니다: " + reviewId));

        if (!review.getUser().getUserNickname().equals(userNickname)) { // userNickname으로 권한 확인
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        // 관련된 모든 이미지 파일 삭제 (물리적 파일 및 DB 엔티티)
        for (ReviewImage image : review.getReviewImages()) {
            reviewImageService.deleteFile(image.getStoredFileName()); // 물리적 파일 삭제
        }
        review.getReviewImages().clear(); // Review 엔티티의 이미지 컬렉션 비우기 (orphanRemoval = true 시 DB에서도 삭제)

        reviewRepository.delete(review); // 리뷰 삭제 (cascade = CascadeType.ALL 및 orphanRemoval = true 설정 시 ReviewImage도 함께 삭제됨)
    }

    // 평균 평점 계산
    public double calculateAverageRating(List<ReviewResponse> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToDouble(r -> r.getRating() != null ? r.getRating().doubleValue() : 0.0)
                .average()
                .orElse(0.0);
    }


    // 필터 카운트 조회
    public Map<String, Integer> getFilterCounts(Long productId) {
        List<Review> reviews;
        if (productId != null) {
            reviews = reviewRepository.findByProduct_ProductId(productId);
        } else {
            reviews = reviewRepository.findAll();
        }

        Map<String, Integer> counts = new HashMap<>();
        counts.put("all", reviews.size());
        counts.put("photo", (int) reviews.stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsPhotoReview()))
                .count());
        counts.put("verified", (int) reviews.stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsVerifiedPurchase()))
                .count());
        return counts;
    }

    // 평점별 카운트 조회
    public Map<String, Integer> getRatingCounts(Long productId) {
        List<Review> reviews;
        if (productId != null) {
            reviews = reviewRepository.findByProduct_ProductId(productId);
        } else {
            reviews = reviewRepository.findAll();
        }

        Map<String, Integer> ratingCounts = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            ratingCounts.put(String.valueOf(i), (int) reviews.stream()
                    .filter(review -> review.getRating() != null
                            && review.getRating().compareTo(BigDecimal.valueOf(rating)) == 0)
                    .count());
        }
        return ratingCounts;
    }


    // 필터 적용 메서드
    private List<Review> applyFilters(List<Review> reviews, String filter, String ratingRange) {
        if ("photo".equals(filter)) {
            reviews = reviews.stream()
                    .filter(r -> Boolean.TRUE.equals(r.getIsPhotoReview()))
                    .collect(Collectors.toList());
        } else if ("verified".equals(filter)) {
            reviews = reviews.stream()
                    .filter(r -> Boolean.TRUE.equals(r.getIsVerifiedPurchase()))
                    .collect(Collectors.toList());
        }

        if (ratingRange != null && !ratingRange.isEmpty()) {
            switch (ratingRange) {
                case "5":
                    reviews = reviews.stream()
                            .filter(r -> r.getRating() != null && r.getRating().compareTo(BigDecimal.valueOf(5)) == 0)
                            .collect(Collectors.toList());
                    break;
                case "4-5":
                    reviews = reviews.stream()
                            .filter(r -> r.getRating() != null
                                    && r.getRating().compareTo(BigDecimal.valueOf(4)) >= 0
                                    && r.getRating().compareTo(BigDecimal.valueOf(5)) <= 0)
                            .collect(Collectors.toList());
                    break;
                case "3-5":
                    reviews = reviews.stream()
                            .filter(r -> r.getRating() != null
                                    && r.getRating().compareTo(BigDecimal.valueOf(3)) >= 0
                                    && r.getRating().compareTo(BigDecimal.valueOf(5)) <= 0)
                            .collect(Collectors.toList());
                    break;
                case "1-2":
                    reviews = reviews.stream()
                            .filter(r -> r.getRating() != null
                                    && r.getRating().compareTo(BigDecimal.valueOf(1)) >= 0
                                    && r.getRating().compareTo(BigDecimal.valueOf(2)) <= 0)
                            .collect(Collectors.toList());
                    break;
            }
        }
        return reviews;
    }


    // 정렬 적용 메서드 (기존 유지)
    private List<Review> applySorting(List<Review> reviews, String sort) {
        switch (sort) {
            case "latest":
                return reviews.stream()
                        .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                        .collect(Collectors.toList());
            case "oldest":
                return reviews.stream()
                        .sorted((r1, r2) -> r1.getCreatedAt().compareTo(r2.getCreatedAt()))
                        .collect(Collectors.toList());
            case "rating-high":
                return reviews.stream()
                        .sorted(Comparator.comparing(Review::getRating, Comparator.nullsLast(BigDecimal::compareTo).reversed()))
                        .collect(Collectors.toList());
            case "rating-low":
                return reviews.stream()
                        .sorted(Comparator.comparing(Review::getRating, Comparator.nullsLast(BigDecimal::compareTo)))
                        .collect(Collectors.toList());
            default:
                return reviews;
        }
    }

    // 페이지 정보 생성 메서드 (기존 유지)
    private Pageable createPageable(int page, int size, String sort) {
        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createdAt";

        switch (sort) {
            case "latest": property = "createdAt"; direction = Sort.Direction.DESC; break;
            case "oldest": property = "createdAt"; direction = Sort.Direction.ASC; break;
            case "rating_high": property = "rating"; direction = Sort.Direction.DESC; break;
            case "rating_low": property = "rating"; direction = Sort.Direction.ASC; break;
        }
        return PageRequest.of(page, size, Sort.by(direction, property));
    }


    public double calculateOverallAverageRating(Long productId) {
        List<Review> reviews = reviewRepository.findByProduct_ProductId(productId);

        //리뷰가 없는 경우 0.0을 반환
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        //Java Stream API를 사용하여 평균 평점을 계산
        return reviews.stream()
                .mapToDouble(review -> review.getRating() != null ? review.getRating().doubleValue() : 0.0)
                .average()
                .orElse(0.0);
    }
}
