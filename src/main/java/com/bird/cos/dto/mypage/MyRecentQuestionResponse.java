package com.bird.cos.dto.mypage;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyRecentQuestionResponse {

    private final Long questionId;
    private final String questionTitle;
    private final LocalDateTime questionCreatedAt;

    public static MyRecentQuestionResponse of(Long questionId, String questionTitle, LocalDateTime createdAt) {
        return MyRecentQuestionResponse.builder()
                .questionId(questionId)
                .questionTitle(questionTitle)
                .questionCreatedAt(createdAt)
                .build();
    }
}

