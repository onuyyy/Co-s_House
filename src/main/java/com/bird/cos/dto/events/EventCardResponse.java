package com.bird.cos.dto.events;

import com.bird.cos.domain.brand.Event;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class EventCardResponse {

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final String slug;
    private final String title;
    private final String summary;
    private final String tag;
    private final String period;
    private final String image;

    public static EventCardResponse from(Event event, EventType eventType) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }

        return EventCardResponse.builder()
                .slug(buildSlug(event.getEventId()))
                .title(event.getEventName())
                .summary(event.getEventDescription())
                .tag(eventType != null ? eventType.getLabel() : null)
                .period(formatPeriod(event))
                .image(normalizeImage(event.getEventImage()))
                .build();
    }

    private static String buildSlug(Long eventId) {
        return eventId == null ? null : "event-" + eventId;
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
