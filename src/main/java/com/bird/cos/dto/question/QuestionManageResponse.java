package com.bird.cos.dto.question;

import com.bird.cos.domain.product.Question;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionManageResponse {

    private Long questionId;
    private Boolean isSecret;
    private String questionContent;
    private LocalDateTime questionCreatedAt;
    private String questionTitle;
    private Long productId;
    private String questionStatus;
    private String questionType;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userPhone;

    public static QuestionManageResponse from(Question question) {
        return new QuestionManageResponse(
                question.getQuestionId(),
                question.getIsSecret(),
                question.getQuestionContent(),
                question.getQuestionCreatedAt(),
                question.getQuestionTitle(),
                question.getProduct() != null ? question.getProduct().getProductId() : null,
                question.getQuestionStatus() != null ? question.getQuestionStatus().getCodeName() : null,
                question.getQuestionType() != null ? question.getQuestionType().getCodeId() : null,
                question.getUser().getUserId(),
                question.getUser().getUserName(),
                question.getUser().getUserEmail(),
                question.getUser().getUserPhone()
        );
    }

    // update
    public QuestionUpdateRequest toUpdateRequest() {
        QuestionUpdateRequest request = new QuestionUpdateRequest();
        request.setQuestionId(this.questionId);
        request.setQuestionTitle(this.questionTitle);
        request.setQuestionContent(this.questionContent);
        request.setQuestionType(this.questionType); // 이미 codeId임
        request.setIsSecret(this.isSecret);
        request.setProductId(this.productId);
        request.setCustomerName(this.userName);
        request.setCustomerEmail(this.userEmail);
        request.setCustomerPhone(this.userPhone);
        return request;
    }

}