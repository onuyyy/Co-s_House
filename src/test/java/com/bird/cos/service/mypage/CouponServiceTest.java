package com.bird.cos.service.mypage;

import com.bird.cos.domain.brand.Brand;
import com.bird.cos.domain.coupon.Coupon;
import com.bird.cos.domain.coupon.CouponScope;
import com.bird.cos.domain.coupon.UserCoupon;
import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.order.MyCouponResponse;
import com.bird.cos.dto.order.SalesPriceCheckResponse;
import com.bird.cos.exception.BusinessException;
import com.bird.cos.repository.mypage.coupon.CouponRepository;
import com.bird.cos.repository.mypage.coupon.UserCouponRepository;
import com.bird.cos.repository.product.ProductRepository;
import com.bird.cos.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCouponRepository userCouponRepository;

    @Mock
    private ProductRepository productRepository;

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

    private User user;
    private Coupon coupon;

    @BeforeEach
    void setUp() {
        couponId1 = 1L;
        couponId2 = 2L;
        orderAmount = new BigDecimal("200");
        productId = 1L;
        brandId = 1L;
        userCouponId = 1L;

              user = User.builder()
                .userId(1L)
                .userEmail("user@example.com")
                .userNickname("tester")
                .userName("테스터")
                .build();

        coupon = new Coupon();
        coupon.setCouponId(10L);
        coupon.setCouponTitle("10% 할인");
        coupon.setScope(CouponScope.GLOBAL);
        coupon.setIsActive(true);
        coupon.setExpiredAt(LocalDateTime.now().plusDays(1));
      
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

    // 활성 쿠폰을 처음 발급받을 때 UserCoupon 저장이 이뤄지는지 확인
    @Test
    void claimCoupon_WhenCouponValidAndNotClaimed_SavesUserCoupon() {
        when(couponRepository.findById(10L)).thenReturn(Optional.of(coupon));
        when(userCouponRepository.existsByUser_UserIdAndCoupon_CouponId(1L, 10L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        couponService.claimCoupon(1L, 10L);

        ArgumentCaptor<UserCoupon> captor = ArgumentCaptor.forClass(UserCoupon.class);
        verify(userCouponRepository).save(captor.capture());
        UserCoupon saved = captor.getValue();
        assertThat(saved.getCoupon()).isEqualTo(coupon);
        assertThat(saved.getUser()).isEqualTo(user);
    }

    // 이미 발급된 쿠폰을 다시 요청하면 예외가 발생하는지 확인
    @Test
    void claimCoupon_WhenAlreadyClaimed_ThrowsIllegalStateException() {
        when(couponRepository.findById(10L)).thenReturn(Optional.of(coupon));
        when(userCouponRepository.existsByUser_UserIdAndCoupon_CouponId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> couponService.claimCoupon(1L, 10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 발급받은 쿠폰입니다.");

        verify(userCouponRepository, never()).save(any());
    }

    // 퍼센트 할인과 최대 할인 한도가 함께 있을 때 정상적으로 상한이 적용되는지 검증
    @Test
    void checkMyCoupon_WithRateDiscountAndMaxCap_ReturnsCappedAmount() {
        coupon.setDiscountRate(BigDecimal.valueOf(10));
        coupon.setDiscountAmount(BigDecimal.ZERO);
        coupon.setMaxDiscountAmount(BigDecimal.valueOf(5000));
        coupon.setMinPurchaseAmount(BigDecimal.valueOf(50000));
        when(couponRepository.findById(10L)).thenReturn(Optional.of(coupon));

        BigDecimal orderAmount = BigDecimal.valueOf(100000);
        SalesPriceCheckResponse response = couponService.checkMyCoupon(10L, orderAmount);

        assertThat(response.isApplicable()).isTrue();
        assertThat(response.getCalculatedDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(response.getFinalAmount()).isEqualByComparingTo(BigDecimal.valueOf(95000));
    }

    // 정액 할인이 주문 금액을 초과할 경우 최종 금액이 0으로 제한되는지 검증
    @Test
    void checkMyCoupon_WithFlatDiscountGreaterThanOrder_CapsAtOrderAmount() {
        coupon.setDiscountRate(BigDecimal.ZERO);
        coupon.setDiscountAmount(BigDecimal.valueOf(20000));
        coupon.setMaxDiscountAmount(BigDecimal.ZERO);
        coupon.setMinPurchaseAmount(BigDecimal.ZERO);
        when(couponRepository.findById(10L)).thenReturn(Optional.of(coupon));

        BigDecimal orderAmount = BigDecimal.valueOf(15000);
        SalesPriceCheckResponse response = couponService.checkMyCoupon(10L, orderAmount);

        assertThat(response.getCalculatedDiscountAmount()).isEqualByComparingTo(orderAmount);
        assertThat(response.getFinalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // 만료된 사용자 쿠폰을 사용할 때 BusinessException이 발생하는지 확인
    @Test
    void checkUserCoupon_WhenExpired_ThrowsBusinessException() {
        coupon.setExpiredAt(LocalDateTime.now().minusDays(1));
        coupon.setIsActive(true);
        UserCoupon userCoupon = UserCoupon.builder()
                .userCouponId(100L)
                .coupon(coupon)
                .user(user)
                .build();
        when(userCouponRepository.findById(100L)).thenReturn(Optional.of(userCoupon));

        assertThatThrownBy(() -> couponService.checkUserCoupon(100L, BigDecimal.TEN))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("만료되었거나 비활성화된 쿠폰입니다.");
    }

    // 이미 사용 처리된 사용자 쿠폰을 사용할 때 예외가 발생하는지 확인
    @Test
    void checkUserCoupon_WhenAlreadyUsed_ThrowsBusinessException() {
        coupon.setExpiredAt(LocalDateTime.now().plusDays(1));
        coupon.setIsActive(true);
        UserCoupon userCoupon = UserCoupon.builder()
                .userCouponId(150L)
                .coupon(coupon)
                .user(user)
                .usedAt(LocalDateTime.now())
                .build();
        when(userCouponRepository.findById(150L)).thenReturn(Optional.of(userCoupon));

        assertThatThrownBy(() -> couponService.checkUserCoupon(150L, BigDecimal.TEN))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 사용된 쿠폰입니다.");
    }

    // 유효한 사용자 쿠폰을 사용할 때 내부적으로 checkMyCoupon 로직이 적용되는지 검증
    @Test
    void checkUserCoupon_WhenValid_DelegatesToCheckMyCoupon() {
        coupon.setDiscountAmount(BigDecimal.valueOf(5000));
        UserCoupon userCoupon = UserCoupon.builder()
                .userCouponId(200L)
                .coupon(coupon)
                .user(user)
                .usedAt(null)
                .build();
        when(userCouponRepository.findById(200L)).thenReturn(Optional.of(userCoupon));
        when(couponRepository.findById(10L)).thenReturn(Optional.of(coupon));

        SalesPriceCheckResponse response = couponService.checkUserCoupon(200L, BigDecimal.valueOf(20000));

        assertThat(response.getCalculatedDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        verify(couponRepository, atLeastOnce()).findById(10L);
    }

    // 상품 목록과 쿠폰 범위에 따라 적용 가능한 쿠폰만 골라내는지 확인
    @Test
    void getApplicableCoupons_WithMixedScopes_ReturnsMatchingCoupons() {
        Brand brand = Brand.builder().brandId(50L).build();
        Product product1 = Product.builder().productId(100L).brand(brand).build();
        Product product2 = Product.builder().productId(200L).brand(brand).build();

        when(productRepository.findAllById(List.of(100L, 200L))).thenReturn(List.of(product1, product2));

        Coupon globalCoupon = new Coupon();
        globalCoupon.setCouponId(1L);
        globalCoupon.setScope(CouponScope.GLOBAL);
        globalCoupon.setExpiredAt(LocalDateTime.now().plusDays(1));
        globalCoupon.setIsActive(true);
        UserCoupon globalUserCoupon = UserCoupon.builder()
                .userCouponId(1L)
                .coupon(globalCoupon)
                .user(user)
                .build();

        Coupon productCoupon = new Coupon();
        productCoupon.setCouponId(2L);
        productCoupon.setScope(CouponScope.PRODUCT);
        productCoupon.setProduct(product1);
        productCoupon.setExpiredAt(LocalDateTime.now().plusDays(1));
        productCoupon.setIsActive(true);
        UserCoupon productUserCoupon = UserCoupon.builder()
                .userCouponId(2L)
                .coupon(productCoupon)
                .user(user)
                .build();

        Coupon nonApplicableCoupon = new Coupon();
        nonApplicableCoupon.setCouponId(3L);
        nonApplicableCoupon.setScope(CouponScope.PRODUCT);
        Product otherProduct = Product.builder().productId(999L).build();
        nonApplicableCoupon.setProduct(otherProduct);
        nonApplicableCoupon.setExpiredAt(LocalDateTime.now().plusDays(1));
        nonApplicableCoupon.setIsActive(true);
        UserCoupon nonApplicableUserCoupon = UserCoupon.builder()
                .userCouponId(3L)
                .coupon(nonApplicableCoupon)
                .user(user)
                .build();

        when(userCouponRepository.findByUser_UserIdAndCoupon_ExpiredAtAfterAndCoupon_IsActive(eq(1L), any(LocalDateTime.class), eq(true)))
                .thenReturn(List.of(globalUserCoupon, productUserCoupon, nonApplicableUserCoupon));

        List<MyCouponResponse> responses = couponService.getApplicableCoupons(1L, List.of(100L, 200L));

        assertThat(responses).hasSize(2);
        assertThat(responses.stream().map(MyCouponResponse::getUserCouponId)).containsExactlyInAnyOrder(1L, 2L);
    }

    // MyCoupons 조회 시 엔티티가 응답 DTO로 정상 변환되는지 검증
    @Test
    void getMyCoupons_WhenRepositoryReturnsCoupons_MapsToResponse() {
        UserCoupon userCoupon = UserCoupon.builder()
                .userCouponId(5L)
                .coupon(coupon)
                .user(user)
                .build();
        when(userCouponRepository.findByUser_UserIdAndCoupon_ExpiredAtAfterAndCoupon_IsActive(eq(1L), any(LocalDateTime.class), eq(true)))
                .thenReturn(Collections.singletonList(userCoupon));

        List<MyCouponResponse> responses = couponService.getMyCoupons(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUserCouponId()).isEqualTo(5L);
    }

    // 사용자 쿠폰 페이지 조회가 Page 형태로 반환되는지 확인
    @Test
    void findUserCoupons_ReturnsPagedResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        UserCoupon userCoupon = UserCoupon.builder()
                .userCouponId(7L)
                .coupon(coupon)
                .user(user)
                .build();
        when(userCouponRepository.findByUser_UserId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(userCoupon)));

        Page<?> result = couponService.findUserCoupons(1L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
    }

    // 사용자 보유 쿠폰 수 조회 시 Repository 값을 그대로 반환하는지 검증
    @Test
    void getMyCouponsCount_ReturnsRepositoryValue() {
        when(userCouponRepository.countByUser_UserIdAndCoupon_IsActive(1L, true)).thenReturn(3L);

        long count = couponService.getMyCouponsCount(1L);

        assertThat(count).isEqualTo(3L);
    }

}
