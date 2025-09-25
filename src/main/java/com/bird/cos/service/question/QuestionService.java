package com.bird.cos.service.question;

import com.bird.cos.domain.product.Question;
import com.bird.cos.domain.user.User;
import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.dto.question.QuestionUpdateRequest;
import com.bird.cos.dto.question.QuestionManageResponse;
import com.bird.cos.repository.common.CommonCodeRepository;
import com.bird.cos.repository.question.QuestionRepository;
import com.bird.cos.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final CommonCodeRepository commonCodeRepository;

    // 문의 저장
    @Transactional
    public Question saveQuestion(QuestionUpdateRequest questionDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 문의 유형 CommonCode 조회
        CommonCode questionType = null;
        if (questionDto.getQuestionType() != null && !questionDto.getQuestionType().isEmpty()) {
            questionType = commonCodeRepository.findById(questionDto.getQuestionType())
                    .orElse(null);
        }

        // 기본 문의 상태 (QS_001 - 답변대기)
        CommonCode questionStatus = commonCodeRepository.findById("QS_001")
                .orElse(null);

        Question question = Question.builder()
                .user(user)
                .questionType(questionType)
                .questionStatus(questionStatus)
                .questionTitle(questionDto.getQuestionTitle())
                .questionContent(questionDto.getQuestionContent())
                .isSecret(questionDto.getIsSecret() != null ? questionDto.getIsSecret() : false)
                .build();

        return questionRepository.save(question);
    }


    //문의 내역 수정
    @Transactional
    public void updateQuestion(Long id, QuestionUpdateRequest request, Long userId) {
        Question existingQuestion = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));

        if(!existingQuestion.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 문의만 수정할 수 있습니다.");
        }

        // 문의 유형 조회
        CommonCode questionType = null;
        if (request.getQuestionType() != null && !request.getQuestionType().isEmpty()) {
            questionType = commonCodeRepository.findById(request.getQuestionType())
                    .orElse(existingQuestion.getQuestionType());
        } else {
            questionType = existingQuestion.getQuestionType();
        }

        // Builder로 새로운 Question 생성
        Question updatedQuestion = Question.builder()
                .questionId(existingQuestion.getQuestionId())
                .user(existingQuestion.getUser())
                .product(existingQuestion.getProduct())
                .questionType(questionType)
                .questionStatus(existingQuestion.getQuestionStatus())
                .questionTitle(request.getQuestionTitle() != null ? request.getQuestionTitle() : existingQuestion.getQuestionTitle())
                .questionContent(request.getQuestionContent() != null ? request.getQuestionContent() : existingQuestion.getQuestionContent())
                .isSecret(request.getIsSecret() != null ? request.getIsSecret() : existingQuestion.getIsSecret())
                .questionCreatedAt(existingQuestion.getQuestionCreatedAt())
                .build();

        questionRepository.save(updatedQuestion);
    }

    @Transactional
    public void deleteQuestion(Long questionId, Long userId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(()-> new IllegalArgumentException("문의를 찾을 수 없습니다."));

        if(!question.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 질문만 삭제할 수 있습니다.");
        }
        questionRepository.delete(question);
    }

    @Transactional(readOnly = true)
    public Page<QuestionManageResponse> getQuestionsByUserId(Long userId, Pageable pageable) {
        Page<Question> questions = questionRepository.findAllByUserIdWithUser(userId, pageable);
        return questions.map(QuestionManageResponse::from);
    }

    @Transactional(readOnly = true)
    public QuestionManageResponse getQuestionDetail(Long questionId, Long userId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));

        if(!question.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 문의만 조회할 수 있습니다.");
        }

        return QuestionManageResponse.from(question);
    }

    // Authentication에서 userId 추출
    public Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }

        String userEmail = authentication.getName();
        if (userEmail == null) {
            throw new IllegalArgumentException("사용자 이메일 정보가 없습니다.");
        }

        User user = userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return user.getUserId();
    }

    // Authentication에서 User 정보 추출 (Controller용)
    public User getUserFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }

        String userEmail = authentication.getName();
        if (userEmail == null) {
            throw new IllegalArgumentException("사용자 이메일 정보가 없습니다.");
        }

        return userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

}