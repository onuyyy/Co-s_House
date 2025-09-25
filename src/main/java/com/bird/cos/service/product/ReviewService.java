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

    // 사용자 권한 확인 (컨트롤러 또는 PreAuthorize에서 처리하는 것이 일반적)
    // 이 메서드는 `updateReview`나 `deleteReview` 내부에서 사용될 수 있습니다.
    private void checkReviewPermission(Long reviewId, String userNickname) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 리뷰입니다: " + reviewId));
        if (!review.getUser().getUserNickname().equals(userNickname)) {
            throw new AccessDeniedException("리뷰에 대한 권한이 없습니다.");
        }
    }

    // 모든 리뷰 조회 (필터링 포함)
    public List<ReviewResponse> findAllReviewsWithFilter(String filter, String sort, String ratingRange, Long productId) {
        // 이 부분은 `findByProduct_ProductId` 같은 메서드를 직접 호출하는 대신,
        // Repository에서 직접 필터링된 Page<Review>를 반환하도록 하는 것이 효율적입니다.
        // 현재는 모든 리뷰를 불러와서 서비스 계층에서 필터링하는 방식.
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

    // 상품별 리뷰 조회 (페이징 포함)
    @Transactional(readOnly = true) // readOnly 명시
    public Map<String, Object> findReviewsByProductIdWithFilterPage(Long productId, String filter, String sort,
                                                                    String ratingRange, Long optionId, int page, int size) {
        Pageable pageable = createPageable(page - 1, size, sort);

        Page<Review> reviewPage;

        // Repository에 복잡한 쿼리를 위임하는 것이 좋습니다.
        // 예를 들어, Querydsl 또는 Spring Data JPA의 Specification을 사용하면 Service 계층의 필터링 로직을 줄일 수 있습니다.
        if (optionId != null) {
            reviewPage = reviewRepository.findByProduct_ProductIdAndProductOption_OptionId(productId, optionId, pageable);
        } else {
            reviewPage = reviewRepository.findByProduct_ProductId(productId, pageable);
        }

        List<Review> reviews = reviewPage.getContent();

        // **주의**: 페이징된 결과를 다시 필터링하면 실제 페이지 크기가 줄어들 수 있습니다.
        // Repository 쿼리 자체에서 필터링을 수행하는 것이 가장 정확합니다.
        reviews = applyFilters(reviews, filter, ratingRange); // 서비스 계층에서 추가 필터링

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

    // 상품별 리뷰 조회 (페이징 없음)
    @Transactional(readOnly = true) // readOnly 명시
    public List<ReviewResponse> findReviewsByProductIdWithFilter(Long productId, String filter, String sort,
                                                                 String ratingRange, Long optionId) {
        // 이 메서드는 `findReviewsByProductIdWithFilterPage`와 중복되므로,
        // 필요하다면 `findReviewsByProductIdWithFilterPage`를 호출하고 모든 페이지를 합치는 방식으로 구현하거나,
        // Repository에서 모든 데이터를 불러오는 전용 메서드를 만드는 것이 좋습니다.
        // 현재 구현은 `largePageSize`를 사용하는데, 데이터가 매우 많으면 성능 문제가 될 수 있습니다.
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

    // 리뷰 생성 (Transactional 추가)
    @Transactional // 쓰기 작업이므로 @Transactional 필요
    public ReviewResponse createReview(Long productId, ReviewRequest requestDto, String userNickname, List<MultipartFile> imageFiles) throws IOException {
        User user = userRepository.findByUserNickname(userNickname)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + userNickname));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다: " + productId));
        ProductOption productOption = productOptionRepository.findById(requestDto.getOptionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 옵션입니다: " + requestDto.getOptionId()));

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

        // `reviewRepository.save(review)` 호출 후 `savedReview`는 `reviewImages` 컬렉션을 포함하지 않을 수 있습니다.
        // 특히 cascade 설정을 사용하지 않거나, 영속성 컨텍스트가 Flush되기 전이라면 그렇습니다.
        // 따라서 이미지가 완전히 로드된 Review 엔티티를 반환하려면 다시 조회하는 것이 안전합니다.
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


        // 변경사항 저장 (review 엔티티는 Dirty Checking에 의해 자동 저장되거나,
        // 관계 변경 시 reviewRepository.save(review)를 호출할 수 있습니다.)
        // 여기서는 명시적으로 호출합니다.
        Review updatedReview = reviewRepository.save(review);

        // DB에서 새로 로드하여 최신 상태 (이미지 컬렉션 포함)를 가져옴
        // (review.getReviewImages()가 Lazy Loading이거나 업데이트 후 바로 반영되지 않을 경우 필요)
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
            case "rating_high":
                return reviews.stream()
                        .sorted((r1, r2) -> r2.getRating().compareTo(r1.getRating()))
                        .collect(Collectors.toList());
            case "rating_low":
                return reviews.stream()
                        .sorted((r1, r2) -> r1.getRating().compareTo(r2.getRating()))
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
}
