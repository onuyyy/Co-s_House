package com.bird.cos.service.question;

import com.bird.cos.domain.proudct.Question;
import com.bird.cos.domain.user.User;
import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.dto.question.QuestionUpdateRequest;
import com.bird.cos.dto.question.QuestionManageResponse;
import java.util.stream.Collectors;
import com.bird.cos.repository.question.QuestionRepository;
import com.bird.cos.repository.user.UserRepository;
import com.bird.cos.repository.CommonCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuestionService 테스트")
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommonCodeRepository commonCodeRepository;

    @InjectMocks
    private QuestionService questionService;

    private User testUser;
    private CommonCode questionType;
    private CommonCode questionStatus;
    private Question testQuestion;
    private QuestionUpdateRequest testQuestionDto;

    @BeforeEach
    void setUp() {
        // Given: 테스트 데이터 준비
        testUser = new User(
                1L, "test@example.com", "password", "테스트닉네임",
                "테스트사용자", "서울시", "010-1234-5678",
                null, null, true, null, null
        );

        questionType = new CommonCode(
                "QT_001", null, "일반문의", "일반문의",
                1, true, null, null
        );

        questionStatus = new CommonCode(
                "QS_001", null, "답변대기", "답변대기",
                1, true, null, null
        );

        testQuestion = Question.builder()
                .questionId(1L)
                .user(testUser)
                .questionType(questionType)
                .questionStatus(questionStatus)
                .questionTitle("테스트 문의")
                .questionContent("테스트 문의 내용")
                .isSecret(false)
                .build();

        testQuestionDto = new QuestionUpdateRequest();
        testQuestionDto.setQuestionTitle("테스트 문의");
        testQuestionDto.setQuestionContent("테스트 문의 내용");
        testQuestionDto.setQuestionType("QT_001");
        testQuestionDto.setIsSecret(false);
        testQuestionDto.setCustomerName("수정된 이름");
        testQuestionDto.setCustomerEmail("modified@example.com");
        testQuestionDto.setCustomerPhone("010-9999-8888");
    }

    @Test
    @DisplayName("문의글을 성공적으로 저장한다")
    void saveQuestion_성공() {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(commonCodeRepository.findById("QT_001")).willReturn(Optional.of(questionType));
        given(commonCodeRepository.findById("QS_001")).willReturn(Optional.of(questionStatus));
        given(questionRepository.save(any(Question.class))).willReturn(testQuestion);

        // When
        Question savedQuestion = questionService.saveQuestion(testQuestionDto);

        // Then
        assertThat(savedQuestion).isNotNull();
        assertThat(savedQuestion.getQuestionTitle()).isEqualTo("테스트 문의");
        assertThat(savedQuestion.getQuestionContent()).isEqualTo("테스트 문의 내용");
        assertThat(savedQuestion.getUser()).isEqualTo(testUser);

        verify(userRepository, times(1)).findById(1L);
        verify(commonCodeRepository, times(1)).findById("QT_001");
        verify(commonCodeRepository, times(1)).findById("QS_001");
        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 문의글 저장 시 예외가 발생한다")
    void saveQuestion_사용자없음_예외발생() {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> questionService.saveQuestion(testQuestionDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(userRepository, times(1)).findById(1L);
        verify(questionRepository, times(0)).save(any(Question.class));
    }



    @Test
    @DisplayName("문의글 ID로 특정 문의글을 조회한다")
    void getQuestionById_성공() {
        // Given
        Long questionId = 1L;
        given(questionRepository.findById(questionId)).willReturn(Optional.of(testQuestion));

        // When
        Optional<Question> result = questionService.getQuestionById(questionId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testQuestion);

        verify(questionRepository, times(1)).findById(questionId);
    }

    @Test
    @DisplayName("존재하지 않는 문의글 ID로 조회 시 빈 Optional을 반환한다")
    void getQuestionById_문의글없음_빈Optional반환() {
        // Given
        Long questionId = 999L;
        given(questionRepository.findById(questionId)).willReturn(Optional.empty());

        // When
        Optional<Question> result = questionService.getQuestionById(questionId);

        // Then
        assertThat(result).isEmpty();

        verify(questionRepository, times(1)).findById(questionId);
    }

    @Test
    @DisplayName("문의글을 성공적으로 수정한다")
    void updateQuestion_성공() {
        // Given
        Long questionId = 1L;
        QuestionUpdateRequest updateDto = new QuestionUpdateRequest();
        updateDto.setQuestionTitle("수정된 제목");
        updateDto.setQuestionContent("수정된 내용");

        given(questionRepository.findById(questionId)).willReturn(Optional.of(testQuestion));

        // When
        questionService.updateQuestion(questionId, updateDto);

        // Then
    }

    @Test
    @DisplayName("존재하지 않는 문의글 수정 시 예외가 발생한다")
    void updateQuestion_문의글없음_예외발생() {
        // Given
        Long questionId = 999L;
        QuestionUpdateRequest updateDto = new QuestionUpdateRequest();
        updateDto.setQuestionTitle("수정된 제목");
        updateDto.setQuestionContent("수정된 내용");

        given(questionRepository.findById(questionId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> questionService.updateQuestion(questionId, updateDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Question not found");

        verify(questionRepository, times(1)).findById(questionId);
        verify(questionRepository, times(0)).save(any(Question.class));
    }

    @Test
    @DisplayName("문의글을 성공적으로 삭제한다")
    void deleteQuestion_성공() {
        // Given
        Long questionId = 1L;

        // When
        questionService.deleteQuestion(questionId);

        // Then
        verify(questionRepository, times(1)).deleteById(questionId);
    }


    @Test
    @DisplayName("페이징을 사용하여 모든 문의글을 조회한다")
    void getAllQuestions_페이징_성공() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Question> questions = Arrays.asList(testQuestion);
        Page<Question> questionPage = new PageImpl<>(questions, pageable, 1);
        given(questionRepository.findAllWithUser(pageable)).willReturn(questionPage);

        // When
        Page<QuestionManageResponse> result = questionService.getQuestionList(pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getQuestionId()).isEqualTo(testQuestion.getQuestionId());

        verify(questionRepository, times(1)).findAllWithUser(pageable);
    }

    @Test
    @DisplayName("페이징을 사용하여 특정 사용자의 문의글을 조회한다")
    void getQuestionsByUserId_페이징_성공() {
        // Given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Question> questions = Arrays.asList(testQuestion);
        Page<Question> questionPage = new PageImpl<>(questions, pageable, 1);
        given(questionRepository.findAllByUserIdWithUser(userId, pageable)).willReturn(questionPage);

        // When
        Page<QuestionManageResponse> result = questionService.getQuestionsByUserId(userId, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getQuestionId()).isEqualTo(testQuestion.getQuestionId());

        verify(questionRepository, times(1)).findAllByUserIdWithUser(userId, pageable);
    }

    @Test
    @DisplayName("문의 저장 시 회원정보가 업데이트된다")
    void saveQuestion_회원정보업데이트_성공() {
        // Given: 기존 사용자와 다른 정보를 가진 DTO
        QuestionUpdateRequest updateDto = new QuestionUpdateRequest();
        updateDto.setQuestionTitle("문의 제목");
        updateDto.setQuestionContent("문의 내용");
        updateDto.setQuestionType("QT_001");
        updateDto.setCustomerName("새로운 이름");
        updateDto.setCustomerEmail("newemail@example.com");
        updateDto.setCustomerPhone("010-5555-6666");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(commonCodeRepository.findById("QT_001")).willReturn(Optional.of(questionType));
        given(commonCodeRepository.findById("QS_001")).willReturn(Optional.of(questionStatus));
        given(questionRepository.save(any(Question.class))).willReturn(testQuestion);

        // When
        Question savedQuestion = questionService.saveQuestion(updateDto);

        // Then
        assertThat(savedQuestion).isNotNull();
        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @Test
    @DisplayName("문의 수정 시 회원정보가 업데이트된다")
    void updateQuestion_회원정보업데이트_성공() {
        // Given
        Long questionId = 1L;
        QuestionUpdateRequest updateDto = new QuestionUpdateRequest();
        updateDto.setQuestionTitle("수정된 제목");
        updateDto.setQuestionContent("수정된 내용");
        updateDto.setCustomerName("수정된 이름");
        updateDto.setCustomerEmail("updated@example.com");
        updateDto.setCustomerPhone("010-7777-8888");

        given(questionRepository.findById(questionId)).willReturn(Optional.of(testQuestion));

        // When
        questionService.updateQuestion(questionId, updateDto);

        // Then
    }
}
