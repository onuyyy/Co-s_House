package com.bird.cos.service.event;

import com.bird.cos.domain.brand.Event;
import com.bird.cos.domain.coupon.Coupon;
import com.bird.cos.domain.coupon.UserCoupon;
import com.bird.cos.domain.user.Point;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.events.EventActionResult;
import com.bird.cos.dto.events.EventCardResponse;
import com.bird.cos.dto.events.EventCouponInfo;
import com.bird.cos.dto.events.EventDetailResponse;
import com.bird.cos.dto.events.EventType;
import com.bird.cos.repository.event.EventRepository;
import com.bird.cos.repository.mypage.coupon.CouponRepository;
import com.bird.cos.repository.mypage.coupon.UserCouponRepository;
import com.bird.cos.repository.user.PointRepository;
import com.bird.cos.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private static final String EVENT_SLUG_PREFIX = "event-";

    private static final String WELCOME_POINT_DESCRIPTION = "웰컴 이벤트 3,000포인트";
    private static final int WELCOME_POINT_AMOUNT = 3_000;

    private final EventRepository eventRepository;
    private final CouponRepository couponRepository;
    private final PointRepository pointRepository;
    private final UserRepository userRepository;
    private final UserCouponRepository userCouponRepository;

    /**
     * 활성화된 이벤트만 조회해 목록으로 변환
     */
    @Transactional(readOnly = true)
    public List<EventCardResponse> getActiveEventCards() {
        return eventRepository.findAll().stream()
                .filter(event -> Boolean.TRUE.equals(event.getIsActive()))
                .map(event -> EventCardResponse.from(event, EventType.from(event.getEventType())))
                .toList();
    }

    /**
     * 상세정보 조회
     */
    @Transactional(readOnly = true)
    public EventDetailResponse getEventDetail(String slug) {
        Long eventId = extractEventIdFromSlug(slug);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이벤트입니다."));

        EventType eventType = EventType.from(event.getEventType());

        List<EventCouponInfo> coupons = event.getBrand() == null
                ? List.of()
                : couponRepository.findByBrand_BrandIdAndIsActiveTrueOrderByExpiredAtAsc(event.getBrand().getBrandId())
                .stream()
                .map(EventCouponInfo::from)
                .toList();

        return EventDetailResponse.from(event, eventType, coupons, false);
    }

    /**
     * 이벤트 유형별 비즈니스 로직을 실행
     */
    @Transactional
    public EventActionResult performEventAction(String slug, Long userId) {
        Long eventId = extractEventIdFromSlug(slug);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이벤트입니다."));

        EventType eventType = EventType.from(event.getEventType());
        if (eventType == null) {
            return EventActionResult.failure("지원하지 않는 이벤트 유형입니다.", false);
        }

        return switch (eventType) {
            case WELCOME -> grantWelcomeBenefit(userId);
            case BRAND -> grantBrandBenefit(event, userId);
            case ATTENDANCE -> EventActionResult.failure("준비 중인 이벤트입니다.", false);
        };
    }

    /**
     * event-아이디에서 숫자 ID를 파싱한다.
     */
    private Long extractEventIdFromSlug(String slug) {
        if (slug == null || !slug.startsWith(EVENT_SLUG_PREFIX)) {
            throw new IllegalArgumentException("올바르지 않은 이벤트 주소입니다.");
        }
        try {
            return Long.parseLong(slug.substring(EVENT_SLUG_PREFIX.length()));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("올바르지 않은 이벤트 주소입니다.");
        }
    }

    /**
     * 웰컴 이벤트 참여 시 포인트를 지급한다.
     */
    private EventActionResult grantWelcomeBenefit(Long userId) {
        boolean alreadyClaimed = pointRepository.existsByUser_UserIdAndPointDescription(userId, WELCOME_POINT_DESCRIPTION);
        if (alreadyClaimed) {
            return EventActionResult.failure("이미 웰컴 혜택을 받으셨습니다.", true);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Point point = Point.builder()
                .user(user)
                .pointAmount(WELCOME_POINT_AMOUNT)
                .pointDescription(WELCOME_POINT_DESCRIPTION)
                .build();
        pointRepository.save(point);

        return EventActionResult.success("웰컴 혜택으로 3,000포인트가 적립되었습니다!", true, WELCOME_POINT_AMOUNT);
    }

    /**
     * 브랜드 이벤트 참여 시 연결된 활성 쿠폰을 모두 발급한다.
     */
    private EventActionResult grantBrandBenefit(Event event, Long userId) {
        if (event.getBrand() == null) {
            return EventActionResult.failure("연결된 브랜드 정보를 찾을 수 없습니다.", false);
        }

        List<Coupon> coupons = couponRepository.findByBrand_BrandIdAndIsActiveTrueOrderByExpiredAtAsc(event.getBrand().getBrandId());
        if (coupons.isEmpty()) {
            return EventActionResult.failure("발급 가능한 쿠폰이 없습니다.", false);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now();
        List<UserCoupon> issueTargets = new ArrayList<>();

        for (Coupon coupon : coupons) {
            if (!isCouponActive(coupon, now)) {
                continue;
            }
            if (userCouponRepository.existsByUser_UserIdAndCoupon_CouponId(userId, coupon.getCouponId())) {
                continue;
            }
            issueTargets.add(UserCoupon.builder()
                    .user(user)
                    .coupon(coupon)
                    .build());
        }

        if (issueTargets.isEmpty()) {
            return EventActionResult.failure("이미 해당 브랜드 쿠폰을 모두 발급받았습니다.", true);
        }

        userCouponRepository.saveAll(issueTargets);

        String brandName = event.getBrand().getBrandName();
        String message = String.format("%s 쿠폰 %d건을 발급했습니다.",
                brandName != null ? brandName : "브랜드",
                issueTargets.size());

        return EventActionResult.success(message, true, null);
    }

    private boolean isCouponActive(Coupon coupon, LocalDateTime now) {
        if (coupon == null || Boolean.FALSE.equals(coupon.getIsActive())) {
            return false;
        }
        if (coupon.getStartDate() != null && coupon.getStartDate().isAfter(now)) {
            return false;
        }
        if (coupon.getExpiredAt() != null && coupon.getExpiredAt().isBefore(now)) {
            return false;
        }
        return true;
    }
}
