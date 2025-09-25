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
            //fetch join 사용 고려
            reviews = reviewRepository.findByProduct_ProductId(productId);
        } else {
            // fetch join 사용 고려 (ex: findAllWithUserAndProductAndImages())
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
        Pageable pageable = createPageable(page - 1, size, sort);

        Page<Review> reviewPage;

        // 필터에 따라 다른 리포지토리 메서드 호출
        if ("photo".equals(filter)) {
            if (optionId != null) {
                reviewPage = reviewRepository.findPhotoReviewsByProductIdAndOptionId(productId, optionId, pageable);
            } else {
                reviewPage = reviewRepository.findPhotoReviewsByProductId(productId, pageable);
            }
        } else {
            if (optionId != null) {
                reviewPage = reviewRepository.findByProduct_ProductIdAndProductOption_OptionId(productId, optionId, pageable);
            } else {
                reviewPage = reviewRepository.findByProduct_ProductId(productId, pageable);
            }
        }

        List<Review> reviews = reviewPage.getContent();

        // 별점 필터만 서비스에서 적용
        if (ratingRange != null && !ratingRange.isEmpty()) {
            reviews = applyRatingFilter(reviews, ratingRange);
        }

        List<ReviewResponse> reviewResponses = reviews.stream()
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

    // 상품별 리뷰 조회 (페이징 없음)
    @Transactional(readOnly = true)
    public List<ReviewResponse> findReviewsByProductIdWithFilter(Long productId, String filter, String sort,
                                                                 String ratingRange, Long optionId) {
        log.info("리뷰 조회 시작 - productId: {}, filter: {}, sort: {}, ratingRange: {}, optionId: {}",
                productId, filter, sort, ratingRange, optionId);

        List<Review> reviews;

        // 옵션 ID로 필터링
        if (optionId != null) {
            reviews = reviewRepository.findByProduct_ProductIdAndProductOption_OptionId(productId, optionId);
        } else {
            reviews = reviewRepository.findByProduct_ProductId(productId);
        }

        log.info("DB에서 조회한 리뷰 수: {}", reviews.size());

        // 필터 적용
        reviews = applyFilters(reviews, filter, ratingRange);
        log.info("필터 적용 후 리뷰 수: {}", reviews.size());

        // 정렬 적용
        reviews = applySorting(reviews, sort);

        List<ReviewResponse> result = reviews.stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());

        log.info("최종 반환할 리뷰 수: {}", result.size());
        return result;
    }


    // 리뷰 생성
    @Transactional // 쓰기 작업
    public ReviewResponse createReview(Long productId, ReviewRequest requestDto, String userNickname, List<MultipartFile> imageFiles) throws IOException {
        User user = userRepository.findByUserNickname(userNickname)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + userNickname));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + productId));
        ProductOption productOption = null;
        if (requestDto.getOptionId() != null) {
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

        log.info("리뷰 수정 시작 - reviewId: {}, 삭제할 이미지 인덱스: {}", reviewId, updateRequest.getDeletedImageIndexes());

        // 1. 리뷰 내용 업데이트
        ProductOption productOption = productOptionRepository.findById(updateRequest.getOptionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 옵션입니다: " + updateRequest.getOptionId()));

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
        // 2-1. 삭제할 기존 이미지 처리 (인덱스 기반)
        if (updateRequest.getDeletedImageIndexes() != null && !updateRequest.getDeletedImageIndexes().isEmpty()) {
            // 현재 리뷰의 이미지 목록을 인덱스 순서로 가져오기
            List<ReviewImage> currentImages = new ArrayList<>(review.getReviewImages());

            // 삭제할 인덱스들을 내림차순 정렬 (뒤에서부터 삭제)
            List<Integer> sortedIndexes = updateRequest.getDeletedImageIndexes().stream()
                    .sorted(Collections.reverseOrder())
                    .collect(Collectors.toList());

            log.info("삭제할 이미지 인덱스 (정렬됨): {}", sortedIndexes);

            for (Integer index : sortedIndexes) {
                if (index >= 0 && index < currentImages.size()) {
                    ReviewImage imageToDelete = currentImages.get(index);
                    log.info("이미지 삭제 중 - 인덱스: {}, 파일명: {}", index, imageToDelete.getStoredFileName());

                    // 물리적 파일 삭제
                    reviewImageService.deleteFile(imageToDelete.getStoredFileName());

                    // DB에서 삭제
                    reviewImageRepository.delete(imageToDelete);

                    // Review 엔티티 컬렉션에서도 제거
                    review.getReviewImages().remove(imageToDelete);
                } else {
                    log.warn("잘못된 이미지 인덱스: {} (전체 이미지 수: {})", index, currentImages.size());
                }
            }
        }

        // 2-2. 새로 업로드할 이미지 처리
        boolean hasNewImageFiles = (updateRequest.getNewImages() != null &&
                !updateRequest.getNewImages().stream().allMatch(MultipartFile::isEmpty));

        if (hasNewImageFiles) {
            List<MultipartFile> actualNewFiles = updateRequest.getNewImages().stream()
                    .filter(f -> !f.isEmpty())
                    .collect(Collectors.toList());

            log.info("새 이미지 파일 수: {}", actualNewFiles.size());

            List<String> newStoredFileNames = reviewImageService.storeFiles(actualNewFiles);
            for (String storedFileName : newStoredFileNames) {
                ReviewImage newReviewImage = new ReviewImage(storedFileName, review);
                review.addReviewImage(newReviewImage);
                reviewImageRepository.save(newReviewImage);
            }
        }

        // 2-3. isPhotoReview 상태 업데이트
        review.setIsPhotoReview(!review.getReviewImages().isEmpty());

        // 변경사항 저장
        Review updatedReview = reviewRepository.save(review);

        log.info("리뷰 수정 완료 - reviewId: {}, 남은 이미지 수: {}", reviewId, updatedReview.getReviewImages().size());

        // DB에서 새로 로드하여 최신 상태를 가져옴
        Review fullyLoadedReview = reviewRepository.findById(updatedReview.getReviewId())
                .orElseThrow(() -> new IllegalStateException("리뷰를 다시 불러올 수 없습니다."));

        return ReviewResponse.fromEntity(fullyLoadedReview);
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId, String userNickname) { // userNickname으로 변경
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 리뷰입니다: " + reviewId));

        if (!review.getUser().getUserNickname().equals(userNickname)) { // userNickname으로 권한 확인
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        // 관련된 모든 이미지 파일 삭제
        for (ReviewImage image : review.getReviewImages()) {
            reviewImageService.deleteFile(image.getStoredFileName()); // 물리적 파일 삭제
        }
        review.getReviewImages().clear(); // Review 엔티티의 이미지 컬렉션 비우기 (orphanRemoval = true 시 DB에서도 삭제)

        reviewRepository.delete(review); // 리뷰 삭제 (cascade = CascadeType.ALL 및 orphanRemoval = true 설정 시 ReviewImage도 함께 삭제됨)
    }

    // 평균 평점 계산
    public double calculateAverageRating(List<ReviewResponse> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            log.debug("리뷰가 없어서 평균 평점 0.0 반환");
            return 0.0;
        }

        double average = reviews.stream()
                .filter(r -> r.getRating() != null)  // null 체크 추가
                .mapToDouble(r -> r.getRating().doubleValue())
                .average()
                .orElse(0.0);

        log.debug("평균 평점 계산 완료 - 리뷰 수: {}, 평균: {}", reviews.size(), average);
        return average;
    }


    // 필터 카운트 조회
    public Map<String, Integer> getFilterCounts(Long productId) {
        List<Review> reviews;
        if (productId != null) {
            reviews = reviewRepository.findByProduct_ProductId(productId);
        } else {
            reviews = reviewRepository.findAll();
        }

        if (reviews == null) {
            reviews = new ArrayList<>();
        }

        Map<String, Integer> counts = new HashMap<>();
        counts.put("all", reviews.size());
        counts.put("photo", (int) reviews.stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsPhotoReview()))
                .count());
        counts.put("verified", (int) reviews.stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsVerifiedPurchase()))
                .count());

        log.debug("필터 카운트 - all: {}, photo: {}, verified: {}",
                counts.get("all"), counts.get("photo"), counts.get("verified"));
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

        if (reviews == null) {
            reviews = new ArrayList<>();
        }

        Map<String, Integer> ratingCounts = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            int count = (int) reviews.stream()
                    .filter(review -> review.getRating() != null
                            && review.getRating().compareTo(BigDecimal.valueOf(rating)) == 0)
                    .count();
            ratingCounts.put(String.valueOf(i), count);
        }

        log.debug("별점 카운트 - 1: {}, 2: {}, 3: {}, 4: {}, 5: {}",
                ratingCounts.get("1"), ratingCounts.get("2"), ratingCounts.get("3"),
                ratingCounts.get("4"), ratingCounts.get("5"));
        return ratingCounts;
    }


    // 필터 적용 메서드
    private List<Review> applyFilters(List<Review> reviews, String filter, String ratingRange) {
        log.info("필터 적용 시작 - filter: {}, ratingRange: {}, 초기 리뷰 수: {}", filter, ratingRange, reviews.size());

        // 기본 필터 적용
        if ("photo".equals(filter)) {
            reviews = reviews.stream()
                    .filter(r -> Boolean.TRUE.equals(r.getIsPhotoReview()))
                    .collect(Collectors.toList());
        } else if ("verified".equals(filter)) {
            reviews = reviews.stream()
                    .filter(r -> Boolean.TRUE.equals(r.getIsVerifiedPurchase()))
                    .collect(Collectors.toList());
        }

        // 별점 필터 적용 (수정된 로직)
        if (ratingRange != null && !ratingRange.isEmpty()) {
            switch (ratingRange) {
                case "5":
                    reviews = reviews.stream()
                            .filter(r -> r.getRating() != null && r.getRating().compareTo(BigDecimal.valueOf(5)) == 0)
                            .collect(Collectors.toList());
                    break;
                case "4":
                    reviews = reviews.stream()
                            .filter(r -> r.getRating() != null && r.getRating().compareTo(BigDecimal.valueOf(4)) == 0)
                            .collect(Collectors.toList());
                    break;
                case "3":
                    reviews = reviews.stream()
                            .filter(r -> r.getRating() != null && r.getRating().compareTo(BigDecimal.valueOf(3)) == 0)
                            .collect(Collectors.toList());
                    break;
                case "2":
                    reviews = reviews.stream()
                            .filter(r -> r.getRating() != null && r.getRating().compareTo(BigDecimal.valueOf(2)) == 0)
                            .collect(Collectors.toList());
                    break;
                case "1":
                    reviews = reviews.stream()
                            .filter(r -> r.getRating() != null && r.getRating().compareTo(BigDecimal.valueOf(1)) == 0)
                            .collect(Collectors.toList());
                    break;
                // 기존 범위 필터들 (필요한 경우)
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

        log.info("필터 적용 완료 - 결과 리뷰 수: {}", reviews.size());
        return reviews;
    }

    // 정렬 적용 메서드 (기존 유지)
    private List<Review> applySorting(List<Review> reviews, String sort) {
        if (reviews.isEmpty()) {
            return reviews;
        }

        switch (sort) {
            case "latest":
                return reviews.stream()
                        .sorted((r1, r2) -> {
                            if (r1.getCreatedAt() == null && r2.getCreatedAt() == null) return 0;
                            if (r1.getCreatedAt() == null) return 1;
                            if (r2.getCreatedAt() == null) return -1;
                            return r2.getCreatedAt().compareTo(r1.getCreatedAt());
                        })
                        .collect(Collectors.toList());
            case "oldest":
                return reviews.stream()
                        .sorted((r1, r2) -> {
                            if (r1.getCreatedAt() == null && r2.getCreatedAt() == null) return 0;
                            if (r1.getCreatedAt() == null) return 1;
                            if (r2.getCreatedAt() == null) return -1;
                            return r1.getCreatedAt().compareTo(r2.getCreatedAt());
                        })
                        .collect(Collectors.toList());
            case "rating-high":
            case "rating_high":
                return reviews.stream()
                        .sorted((r1, r2) -> {
                            if (r1.getRating() == null && r2.getRating() == null) return 0;
                            if (r1.getRating() == null) return 1;
                            if (r2.getRating() == null) return -1;
                            return r2.getRating().compareTo(r1.getRating());
                        })
                        .collect(Collectors.toList());
            case "rating-low":
            case "rating_low":
                return reviews.stream()
                        .sorted((r1, r2) -> {
                            if (r1.getRating() == null && r2.getRating() == null) return 0;
                            if (r1.getRating() == null) return 1;
                            if (r2.getRating() == null) return -1;
                            return r1.getRating().compareTo(r2.getRating());
                        })
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

            case "rating-high":
            case "rating_high":
                property = "rating";
                direction = Sort.Direction.DESC;
                break;
            case "rating-low":
            case "rating_low":
                property = "rating";
                direction = Sort.Direction.ASC;
                break;
        }
        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}