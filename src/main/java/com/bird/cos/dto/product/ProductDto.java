package com.bird.cos.dto.product;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor //기본생성자
@AllArgsConstructor // 모든 필드를 매개변수로 하는 생성자
public class ProductDto {

    private Long productId; // 상품 ID
    private String productTitle; // 상품명
    private Long brandId; // 브랜드 ID
    private Long productCategoryId; // 상품 카테고리 ID
    private String mainImageUrl; // 메인 이미지 URL
    private String description; // 상품 설명
    private BigDecimal originalPrice; // 정가
    private BigDecimal salePrice; // 할인가
    private BigDecimal couponPrice; // 쿠폰 적용가
    private BigDecimal discountRate; // 할인율
    private Boolean isFreeShipping; // 무료배송 여부
    private Boolean isTodayDeal; // 오늘의딜 여부
    private Boolean isCohouseOnly; // 오늘의집 단독상품 여부
    private String productColor; // 주요 색상
    private String material; // 소재
    private String capacity; // 사용 인원/용량
    private Integer stockQuantity; // 재고수량
    private Long viewCount; // 조회수
    private Long salesCount; // 판매량
    private Integer reviewCount; // 리뷰 개수
    private BigDecimal averageRating; // 평균평점
    private Integer bookmarkCount; // 북마크수
    private String productStatus; // 상품상태
    private LocalDateTime productCreatedAt; // 상품 생성일
    private LocalDateTime productUpdatedAt; // 상품 수정일
}