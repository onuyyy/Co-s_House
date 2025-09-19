package com.bird.cos.dto.question;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionUpdateRequest {

    private Long questionId;
    private String questionTitle;
    private String questionContent;
    private String questionType;
    private Boolean isSecret;
    private Long productId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
}