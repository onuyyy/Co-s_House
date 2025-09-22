// *2. CommonCode 연결, user_id=1 하드코딩, questionStatus=QS_001 기본값, 조회 메서드들 추가
package com.bird.cos.service.question;

import com.bird.cos.domain.product.Question;
import com.bird.cos.domain.user.User;
import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.dto.question.QuestionUpdateRequest;
import com.bird.cos.dto.question.QuestionManageResponse;
import com.bird.cos.repository.question.QuestionRepository;
import com.bird.cos.repository.user.UserRepository;
import com.bird.cos.repository.CommonCodeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final CommonCodeRepository commonCodeRepository;

    public QuestionService(QuestionRepository questionRepository, UserRepository userRepository, CommonCodeRepository commonCodeRepository) {
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.commonCodeRepository = commonCodeRepository;
    }

    // 문의 저장 (user_id = 1로 하드코딩)
    @Transactional
    public Question saveQuestion(QuestionUpdateRequest questionDto) {
        // 첫 번째 사용자로 하드코딩
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 회원 정보 업데이트 (이메일, 연락처가 변경된 경우)
        updateUserInfo(user, questionDto);

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


    // 특정 문의 ID로 조회
    @Transactional(readOnly = true)
    public Optional<Question> getQuestionById(Long questionId) {
        return questionRepository.findById(questionId);
    }


    //문의 내역 수정
    @Transactional
    public void updateQuestion(Long id, QuestionUpdateRequest request) {
        Question existingQuestion = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        // 회원 정보 업데이트
        User updatedUser = updateUserInfo(existingQuestion.getUser(), request);

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
                .user(updatedUser)
                .product(existingQuestion.getProduct())
                .questionType(questionType)
                .questionStatus(existingQuestion.getQuestionStatus())
                .questionTitle(request.getQuestionTitle() != null ? request.getQuestionTitle() : existingQuestion.getQuestionTitle())
                .questionContent(request.getQuestionContent() != null ? request.getQuestionContent() : existingQuestion.getQuestionContent())
                .isSecret(request.getIsSecret() != null ? request.getIsSecret() : false)
                .questionCreatedAt(existingQuestion.getQuestionCreatedAt())
                .build();

        questionRepository.save(updatedQuestion);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<QuestionManageResponse> getQuestionList(Pageable pageable) {
        Page<Question> questions = questionRepository.findAllWithUser(pageable);
        return questions.map(QuestionManageResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<QuestionManageResponse> getQuestionsByUserId(Long userId, Pageable pageable) {
        Page<Question> questions = questionRepository.findAllByUserIdWithUser(userId, pageable);
        return questions.map(QuestionManageResponse::from);
    }

    @Transactional(readOnly = true)
    public QuestionManageResponse getQuestionDetail(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));
        return QuestionManageResponse.from(question);
    }


    // 회원 정보 업데이트 (Builder 방식)
    private User updateUserInfo(User user, QuestionUpdateRequest request) {
        // 업데이트할 값들 확인
        String newEmail = request.getCustomerEmail();
        String newPhone = request.getCustomerPhone();
        String newName = request.getCustomerName();

        // 변경이 필요한 경우에만 새로운 User 생성
        boolean needsUpdate = false;

        if (newEmail != null && !newEmail.trim().isEmpty() && !newEmail.equals(user.getUserEmail())) {
            needsUpdate = true;
        }
        if (newPhone != null && !newPhone.trim().isEmpty() && !newPhone.equals(user.getUserPhone())) {
            needsUpdate = true;
        }
        if (newName != null && !newName.trim().isEmpty() && !newName.equals(user.getUserName())) {
            needsUpdate = true;
        }

        if (needsUpdate) {
            User updatedUser = User.builder()
                    .userId(user.getUserId())
                    .socialId(user.getSocialId())
                    .socialProvider(user.getSocialProvider())
                    .termsAgreed(user.getTermsAgreed())
                    .userAddress(user.getUserAddress())
                    .userCreatedAt(user.getUserCreatedAt())
                    .userEmail(newEmail != null && !newEmail.trim().isEmpty() ? newEmail.trim() : user.getUserEmail())
                    .userName(newName != null && !newName.trim().isEmpty() ? newName.trim() : user.getUserName())
                    .userNickname(user.getUserNickname())
                    .userPassword(user.getUserPassword())
                    .userPhone(newPhone != null && !newPhone.trim().isEmpty() ? newPhone.trim() : user.getUserPhone())
                    .userUpdatedAt(user.getUserUpdatedAt())
                    .emailVerified(user.isEmailVerified())
                    .build();

            return userRepository.save(updatedUser);
        }

        return user;
    }
}
