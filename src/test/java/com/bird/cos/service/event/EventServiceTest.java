package com.bird.cos.service.event;

import com.bird.cos.domain.brand.Brand;
import com.bird.cos.domain.brand.Event;
import com.bird.cos.domain.coupon.Coupon;
import com.bird.cos.domain.coupon.UserCoupon;
import com.bird.cos.domain.user.Point;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.events.EventActionResult;
import com.bird.cos.dto.events.EventCardResponse;
import com.bird.cos.dto.events.EventDetailResponse;
import com.bird.cos.dto.events.EventType;
import com.bird.cos.repository.event.EventRepository;
import com.bird.cos.repository.mypage.coupon.CouponRepository;
import com.bird.cos.repository.mypage.coupon.UserCouponRepository;
import com.bird.cos.repository.user.PointRepository;
import com.bird.cos.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private CouponRepository couponRepository;
    @Mock
    private PointRepository pointRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserCouponRepository userCouponRepository;
    @Mock
    private ResourceLoader resourceLoader;

    @InjectMocks
    private EventService eventService;

    private Brand brand;
    private User user;

    @BeforeEach
    void setUp() {
        brand = Brand.builder()
                .brandId(1L)
                .brandName("테스트 브랜드")
                .build();

        user = User.builder()
                .userId(10L)
                .userEmail("user@example.com")
                .userNickname("tester")
                .userName("테스터")
                .build();

        lenient().when(resourceLoader.getResource(anyString())).thenAnswer(invocation -> {
            Resource resource = mock(Resource.class);
            when(resource.exists()).thenReturn(false);
            return resource;
        });
    }

    // 활성화된 이벤트만 카드 목록으로 변환되는지 확인
    @Test
    void getActiveEventCards_FiltersInactiveAndMapsToResponses() {
        LocalDateTime now = LocalDateTime.now();
        Event active = Event.builder()
                .eventId(1L)
                .eventName("웰컴 이벤트")
                .eventType(EventType.WELCOME.name())
                .eventDescription("설명")
                .eventImage("/event-banner.png")
                .eventStartDate(now.minusDays(1))
                .eventEndDate(now.plusDays(1))
                .isActive(true)
                .build();
        Event inactive = Event.builder()
                .eventId(2L)
                .eventName("비활성 이벤트")
                .eventType(EventType.BRAND.name())
                .eventDescription("설명")
                .eventStartDate(now.minusDays(2))
                .eventEndDate(now.plusDays(2))
                .isActive(false)
                .build();
        when(eventRepository.findAll()).thenReturn(List.of(active, inactive));

        List<EventCardResponse> cards = eventService.getActiveEventCards();

        assertThat(cards).hasSize(1);
        EventCardResponse card = cards.get(0);
        assertThat(card.getSlug()).isEqualTo("event-1");
        assertThat(card.getTitle()).isEqualTo("웰컴 이벤트");
        assertThat(card.getImage()).isEqualTo("/event-banner.png");
    }

    // 이벤트 슬러그로 상세 조회 시 쿠폰 정보와 기본 이미지가 포함되는지 확인
    @Test
    void getEventDetail_WhenSlugValid_ReturnsDetailWithCoupons() {
        LocalDateTime now = LocalDateTime.now();
        Event event = Event.builder()
                .eventId(5L)
                .eventName("브랜드 이벤트")
                .eventType(EventType.BRAND.name())
                .brand(brand)
                .eventDescription("브랜드 혜택")
                .eventImage("   ")
                .eventStartDate(now.minusDays(1))
                .eventEndDate(now.plusDays(5))
                .isActive(true)
                .build();

        Coupon coupon = new Coupon();
        coupon.setCouponId(100L);
        coupon.setCouponTitle("10% 할인");
        coupon.setCouponDescription("설명");
        coupon.setStartDate(now.minusDays(1));
        coupon.setExpiredAt(now.plusDays(7));
        coupon.setIsActive(true);

        when(eventRepository.findById(5L)).thenReturn(Optional.of(event));
        when(couponRepository.findByBrand_BrandIdAndIsActiveTrueOrderByExpiredAtAsc(1L))
                .thenReturn(List.of(coupon));

        EventDetailResponse detail = eventService.getEventDetail("event-5");

        assertThat(detail.getSlug()).isEqualTo("event-5");
        assertThat(detail.getTitle()).isEqualTo("브랜드 이벤트");
        assertThat(detail.getCoupons()).hasSize(1);
        assertThat(detail.getImage()).isEqualTo("/images/home.jpeg");
    }

    // 잘못된 슬러그로 상세 조회를 요청하면 예외가 발생하는지 확인
    @Test
    void getEventDetail_WhenSlugInvalid_ThrowsException() {
        assertThatThrownBy(() -> eventService.getEventDetail("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("올바르지 않은 이벤트 주소입니다.");
    }

    // 웰컴 이벤트 참여 시 포인트가 적립되고 성공 응답을 반환하는지 확인
    @Test
    void performEventAction_WelcomeEvent_GrantsPointOnce() {
        Event event = Event.builder()
                .eventId(3L)
                .eventName("웰컴")
                .eventType(EventType.WELCOME.name())
                .build();

        when(eventRepository.findById(3L)).thenReturn(Optional.of(event));
        when(pointRepository.existsByUser_UserIdAndPointDescription(eq(10L), anyString()))
                .thenReturn(false);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(pointRepository.save(any(Point.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventActionResult result = eventService.performEventAction("event-3", 10L);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPointAmount()).isEqualTo(5_000);
        ArgumentCaptor<Point> captor = ArgumentCaptor.forClass(Point.class);
        verify(pointRepository).save(argThat(saved -> saved.getPointAmount().equals(5_000)));
    }

    // 웰컴 이벤트를 이미 참여한 사용자는 실패 응답과 completed=true를 받는지 확인
    @Test
    void performEventAction_WelcomeEventAlreadyClaimed_ReturnsFailure() {
        Event event = Event.builder()
                .eventId(4L)
                .eventName("웰컴")
                .eventType(EventType.WELCOME.name())
                .build();
        when(eventRepository.findById(4L)).thenReturn(Optional.of(event));
        when(pointRepository.existsByUser_UserIdAndPointDescription(eq(10L), anyString()))
                .thenReturn(true);

        EventActionResult result = eventService.performEventAction("event-4", 10L);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isCompleted()).isTrue();
        verify(pointRepository, never()).save(any());
    }

    // 브랜드 이벤트 참여 시 활성 쿠폰 중 미발급분만 발급되고 메시지가 생성되는지 확인
    @Test
    void performEventAction_BrandEvent_IssuesAvailableCoupons() {
        LocalDateTime now = LocalDateTime.now();
        Event event = Event.builder()
                .eventId(6L)
                .eventName("브랜드 이벤트")
                .eventType(EventType.BRAND.name())
                .brand(brand)
                .build();

        Coupon activeCoupon = new Coupon();
        activeCoupon.setCouponId(101L);
        activeCoupon.setStartDate(now.minusDays(1));
        activeCoupon.setExpiredAt(now.plusDays(5));
        activeCoupon.setIsActive(true);

        Coupon alreadyClaimed = new Coupon();
        alreadyClaimed.setCouponId(102L);
        alreadyClaimed.setStartDate(now.minusDays(1));
        alreadyClaimed.setExpiredAt(now.plusDays(5));
        alreadyClaimed.setIsActive(true);

        Coupon inactiveCoupon = new Coupon();
        inactiveCoupon.setCouponId(103L);
        inactiveCoupon.setStartDate(now.plusDays(1));
        inactiveCoupon.setExpiredAt(now.plusDays(10));
        inactiveCoupon.setIsActive(true);

        when(eventRepository.findById(6L)).thenReturn(Optional.of(event));
        when(couponRepository.findByBrand_BrandIdAndIsActiveTrueOrderByExpiredAtAsc(1L))
                .thenReturn(List.of(activeCoupon, alreadyClaimed, inactiveCoupon));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(userCouponRepository.existsByUser_UserIdAndCoupon_CouponId(10L, 101L)).thenReturn(false);
        when(userCouponRepository.existsByUser_UserIdAndCoupon_CouponId(10L, 102L)).thenReturn(true);

        EventActionResult result = eventService.performEventAction("event-6", 10L);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("테스트 브랜드");
        verify(userCouponRepository).saveAll(argThat(saved -> {
            if (saved instanceof List<UserCoupon> list) {
                assertThat(list).hasSize(1);
                assertThat(list.get(0).getCoupon()).isEqualTo(activeCoupon);
            } else {
                assertThat(saved).hasSize(1);
                assertThat(saved.iterator().next().getCoupon()).isEqualTo(activeCoupon);
            }
            return true;
        }));
    }

    // 브랜드 이벤트에서 발급 가능한 쿠폰이 없으면 실패 응답을 반환하는지 확인
    @Test
    void performEventAction_BrandEventWithoutCoupons_ReturnsFailure() {
        Event event = Event.builder()
                .eventId(7L)
                .eventName("브랜드 이벤트")
                .eventType(EventType.BRAND.name())
                .brand(brand)
                .build();
        when(eventRepository.findById(7L)).thenReturn(Optional.of(event));
        when(couponRepository.findByBrand_BrandIdAndIsActiveTrueOrderByExpiredAtAsc(1L))
                .thenReturn(List.of());

        EventActionResult result = eventService.performEventAction("event-7", 10L);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("발급 가능한 쿠폰이 없습니다.");
    }

    // 지원하지 않는 이벤트 유형이면 실패 응답을 반환하는지 확인
    @Test
    void performEventAction_UnsupportedType_ReturnsFailure() {
        Event event = Event.builder()
                .eventId(8L)
                .eventName("테스트 이벤트")
                .eventType("UNKNOWN")
                .build();
        when(eventRepository.findById(8L)).thenReturn(Optional.of(event));

        EventActionResult result = eventService.performEventAction("event-8", 10L);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("지원하지 않는 이벤트 유형입니다.");
    }

    // 브랜드 정보가 없는 브랜드 이벤트는 실패 응답을 반환하는지 확인
    @Test
    void performEventAction_BrandEventWithoutBrand_ReturnsFailure() {
        Event event = Event.builder()
                .eventId(9L)
                .eventName("브랜드 이벤트")
                .eventType(EventType.BRAND.name())
                .brand(null)
                .build();
        when(eventRepository.findById(9L)).thenReturn(Optional.of(event));

        EventActionResult result = eventService.performEventAction("event-9", 10L);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("연결된 브랜드 정보를 찾을 수 없습니다.");
    }

    // 잘못된 슬러그로 이벤트 액션을 시도하면 예외를 던지는지 확인
    @Test
    void performEventAction_WhenSlugInvalid_ThrowsException() {
        assertThatThrownBy(() -> eventService.performEventAction("bad", 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("올바르지 않은 이벤트 주소입니다.");
    }
}
