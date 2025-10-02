package com.bird.cos.dto.events;

import com.bird.cos.domain.brand.Event;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Builder(toBuilder = true)
public class EventDetailResponse {

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final String DEFAULT_SUBTITLE = "코스 하우스 이벤트";

    private final String slug;
    private final String title;
    private final String subtitle;
    private final String description;
    private final String tag;
    private final String image;
    private final String period;
    private final String benefitText;
    private final boolean loginRequired;
    private final boolean alreadyCompleted;
    private final String actionLabel;
    private final EventType type;
    private final List<EventCouponInfo> coupons;
    private final Long brandId;

    public static EventDetailResponse from(Event event,
                                           EventType eventType,
                                           List<EventCouponInfo> coupons,
                                           boolean alreadyCompleted) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }

        List<EventCouponInfo> safeCoupons = coupons == null ? List.of() : List.copyOf(coupons);

        return EventDetailResponse.builder()
                .slug(buildSlug(event.getEventId()))
                .title(event.getEventName())
                .subtitle(resolveSubtitle(event))
                .description(event.getEventDescription())
                .tag(eventType != null ? eventType.getLabel() : null)
                .image(normalizeImage(event.getEventImage()))
                .period(formatPeriod(event))
                .benefitText(event.getEventDescription())
                .loginRequired(eventType != null && eventType.isLoginRequired())
                .alreadyCompleted(alreadyCompleted)
                .actionLabel(eventType != null ? eventType.getActionLabel() : "참여하기")
                .type(eventType)
                .coupons(safeCoupons)
                .brandId(event.getBrand() != null ? event.getBrand().getBrandId() : null)
                .build();
    }

    private static String buildSlug(Long eventId) {
        return eventId == null ? null : "event-" + eventId;
    }

    private static String resolveSubtitle(Event event) {
        if (event.getBrand() != null) {
            String name = event.getBrand().getBrandName();
            if (name != null) {
                String trimmed = name.trim();
                if (!trimmed.isEmpty()) {
                    return trimmed;
                }
            }
        }
        return DEFAULT_SUBTITLE;
    }

    private static String formatPeriod(Event event) {
        if (event.getEventStartDate() == null || event.getEventEndDate() == null) {
            return null;
        }
        return PERIOD_FORMATTER.format(event.getEventStartDate()) + " ~ " +
                PERIOD_FORMATTER.format(event.getEventEndDate());
    }

    private static String normalizeImage(String image) {
        if (image == null) {
            return null;
        }
        String trimmed = image.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
