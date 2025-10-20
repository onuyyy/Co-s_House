package com.bird.cos.service.mypage;

import com.bird.cos.domain.brand.Brand;
import com.bird.cos.domain.coupon.Coupon;
import com.bird.cos.domain.coupon.UserCoupon;
import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.order.SalesPriceCheckResponse;
import com.bird.cos.exception.BusinessException;
import com.bird.cos.repository.mypage.coupon.CouponRepository;
import com.bird.cos.repository.mypage.coupon.UserCouponRepository;
import com.bird.cos.repository.product.ProductRepository;
import com.bird.cos.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock private CouponRepository couponRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserCouponRepository userCouponRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private CouponService couponService;

    private Long couponId1;
    private Long couponId2;
    private Long productId;
    private Long brandId;
    private BigDecimal orderAmount;
    private Long userCouponId;

    private Coupon mockCoupon1;
    private Coupon mockCoupon2;
    private Brand mockBrand;
    private Product mockProduct;
    private UserCoupon mockUserCoupon;
    private User mockUser;

    @BeforeEach
    void setUp() {
        couponId1 = 1L;
        couponId2 = 2L;
        orderAmount = new BigDecimal("200");
        productId = 1L;
        brandId = 1L;
        userCouponId = 1L;

        mockBrand = Brand.builder()
                .brandId(brandId)
                .brandName("test-brand")
                .build();

        mockProduct = Product.builder()
                .productId(productId)
                .productTitle("테스트 상품")
                .mainImageUrl("http://test-image.jpg")
                .originalPrice(BigDecimal.valueOf(10000))
                .build();

        mockCoupon1 = Coupon.builder()
                .couponId(couponId1)
                .couponTitle("쿠폰이름")
                .product(mockProduct)
                .discountRate(BigDecimal.valueOf(10))
                .discountAmount(BigDecimal.valueOf(20))
                .maxDiscountAmount(BigDecimal.valueOf(100))
                .minPurchaseAmount(BigDecimal.valueOf(10))
                .expiredAt(LocalDateTime.now().plusDays(7)) // 유효한 쿠폰
                .isActive(true)
                .build();

        mockCoupon2 = Coupon.builder()
                .couponId(couponId2)
                .couponTitle("쿠폰이름")
                .brand(mockBrand)
                .discountRate(BigDecimal.valueOf(10))
                .discountAmount(BigDecimal.valueOf(20))
                .maxDiscountAmount(BigDecimal.valueOf(100))
                .minPurchaseAmount(BigDecimal.valueOf(10))
                .build();

        mockUser = User.builder()
                .userName("test-userName")
                .userId(1L)
                .build();

        mockUserCoupon = UserCoupon.builder()
                .userCouponId(userCouponId)
                .user(mockUser)
                .coupon(mockCoupon1)
                .build();
    }

    @Test
    void givenCouponIdAndOrderAmount_whenCheckMyCoupon_thenSalesPriceCheckResponse() {
        // given
        when(couponRepository.findById(couponId1)).thenReturn(Optional.of(mockCoupon1));

        // when
        SalesPriceCheckResponse result = couponService.checkMyCoupon(couponId1, orderAmount);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isApplicable()).isTrue();
        assertThat(result.getCalculatedDiscountAmount().compareTo(BigDecimal.valueOf(20.00))).isZero(); // 10% of 200 = 20
        assertThat(result.getFinalAmount().compareTo(BigDecimal.valueOf(180.00))).isZero();// 200 - 20 = 180
    }

    @Test
    void givenUserCouponIdAndOrderAmount_whenCheckMyCoupon_thenSalesPriceCheckResponse() {
        // given
        when(userCouponRepository.findById(userCouponId)).thenReturn(Optional.of(mockUserCoupon));
        when(couponRepository.findById(couponId1)).thenReturn(Optional.of(mockCoupon1));

        // when
        SalesPriceCheckResponse result = couponService.checkUserCoupon(userCouponId, orderAmount);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isApplicable()).isTrue();
        assertThat(result.getDiscountRate()).isEqualTo(BigDecimal.valueOf(10));
        verify(userCouponRepository, times(1)).findById(userCouponId);
        verify(couponRepository, times(1)).findById(couponId1);

    }

    @Test
    void givenInvalidCouponId_whenCheckMyCoupon_thenThrowsException() {
        when(couponRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> couponService.checkMyCoupon(999L, orderAmount))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void givenExpiredUserCoupon_whenCheckUserCoupon_thenThrowsException() {
        mockUserCoupon.getCoupon().setExpiredAt(LocalDateTime.now().minusDays(1));
        when(userCouponRepository.findById(userCouponId)).thenReturn(Optional.of(mockUserCoupon));

        assertThatThrownBy(() -> couponService.checkUserCoupon(userCouponId, orderAmount))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("만료되었거나 비활성화된 쿠폰입니다");
    }

}