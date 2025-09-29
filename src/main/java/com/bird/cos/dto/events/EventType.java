package com.bird.cos.dto.events;

import lombok.Getter;

import java.util.Locale;

@Getter
public enum EventType {
    WELCOME("웰컴", "웰컴 혜택 받기", true),
    BRAND("브랜드", "혜택 받기", false),
    ATTENDANCE("출석", "출석 체크", true);

    private final String label;
    private final String actionLabel;
    private final boolean loginRequired;

    EventType(String label, String actionLabel, boolean loginRequired) {
        this.label = label;
        this.actionLabel = actionLabel;
        this.loginRequired = loginRequired;
    }

    public static EventType from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return EventType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
