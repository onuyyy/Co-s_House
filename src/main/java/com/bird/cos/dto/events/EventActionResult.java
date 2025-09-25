package com.bird.cos.dto.events;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventActionResult {

    private final boolean success;
    private final String message;
    private final boolean completed;
    private final Integer pointAmount;

    public static EventActionResult success(String message, boolean completed, Integer pointAmount) {
        return EventActionResult.builder()
                .success(true)
                .message(message)
                .completed(completed)
                .pointAmount(pointAmount)
                .build();
    }

    public static EventActionResult failure(String message, boolean completed) {
        return EventActionResult.builder()
                .success(false)
                .message(message)
                .completed(completed)
                .build();
    }
}
