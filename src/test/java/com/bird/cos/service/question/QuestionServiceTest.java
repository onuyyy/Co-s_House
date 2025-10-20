package com.bird.cos.service.question;

import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.product.Question;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.question.QuestionManageResponse;
import com.bird.cos.dto.question.QuestionUpdateRequest;
import com.bird.cos.repository.common.CommonCodeRepository;
import com.bird.cos.repository.product.ProductRepository;
import com.bird.cos.repository.question.QuestionRepository;
import com.bird.cos.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommonCodeRepository commonCodeRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private QuestionService questionService;

    private User user;
    private Product product;
    private CommonCode questionType;
    private CommonCode questionStatus;

    @BeforeEach
    void setUp(){
        user = User.builder()
                .userId(1L)
                .userEmail("test@test.com")
                .userNickname("tester")
                .userName("테스터")
                .build();

        product = Product.builder()
                .productId(100L)
                .productTitle("테스트 상품")
                .build();

        questionType = CommonCode.builder()
                .codeId("QT_001")
                .codeName("상품 문의")
                .build();

        questionStatus = CommonCode.builder()
                .codeId("QS_001")
                .codeName("답변 대기")
                .build();
    }

    @Test
    void saveQuestion_상품문의_저장_성공(){
        // Given
        QuestionUpdateRequest request = new QuestionUpdateRequest();
        request.setProductId(100L);
        request.setQuestionType("QT_001");
        request.setQuestionTitle("상품 문의합니다.");
        request.setQuestionContent("언제 배송되나요?");
        request.setIsSecret(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(commonCodeRepository.findById("QT_001")).thenReturn(Optional.of(questionType));
        when(commonCodeRepository.findById("QS_001")).thenReturn(Optional.of(questionStatus));

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        when(questionRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Question result = questionService.saveQuestion(request, 1L);

        // Then
        Question saved = captor.getValue();
        assertThat(saved.getQuestionTitle()).isEqualTo("상품 문의합니다.");
        assertThat(saved.getQuestionContent()).isEqualTo("언제 배송되나요?");
        assertThat(saved.getProduct()).isEqualTo(product);
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getIsSecret()).isFalse();
        assertThat(saved.getQuestionType()).isEqualTo(questionType);
        assertThat(saved.getQuestionStatus()).isEqualTo(questionStatus);

        verify(questionRepository).save(any(Question.class));
    }

    @Test
    void saveQuestion_일반문의_저장_성공() {
        // Given
        QuestionUpdateRequest request = new QuestionUpdateRequest();
        request.setProductId(null);
        request.setQuestionType("QT_002");
        request.setQuestionTitle("일반 문의입니다");
        request.setQuestionContent("회원 탈퇴는 어떻게 하나요?");
        request.setIsSecret(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commonCodeRepository.findById("QT_002")).thenReturn(Optional.of(questionType));
        when(commonCodeRepository.findById("QS_001")).thenReturn(Optional.of(questionStatus));

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        when(questionRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Question result = questionService.saveQuestion(request, 1L);

        // Then
        Question saved = captor.getValue();
        assertThat(saved.getProduct()).isNull();
        assertThat(saved.getQuestionTitle()).isEqualTo("일반 문의입니다");
        assertThat(saved.getIsSecret()).isTrue();

        verify(productRepository, never()).findById(any());
    }

    @Test
    void saveQuestion_존재하지않는사용자_예외발생() {
        // Given
        QuestionUpdateRequest request = new QuestionUpdateRequest();
        request.setQuestionTitle("문의");
        request.setQuestionContent("내용");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> questionService.saveQuestion(request, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(questionRepository, never()).save(any());
    }

    @Test
    void saveQuestion_존재하지않는상품_예외발생() {
        // Given
        QuestionUpdateRequest request = new QuestionUpdateRequest();
        request.setProductId(999L);
        request.setQuestionTitle("문의");
        request.setQuestionContent("내용");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> questionService.saveQuestion(request, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품을 찾을 수 없습니다. ID: 999");

        verify(questionRepository, never()).save(any());
    }

    @Test
    void updateQuestion_본인문의수정_성공() {
        // Given
        Long questionId = 1L;
        Question existingQuestion = Question.builder()
                .questionId(questionId)
                .user(user)
                .product(product)
                .questionType(questionType)
                .questionStatus(questionStatus)
                .questionTitle("원래 제목")
                .questionContent("원래 내용")
                .isSecret(false)
                .build();

        QuestionUpdateRequest request = new QuestionUpdateRequest();
        request.setQuestionTitle("수정된 제목");
        request.setQuestionContent("수정된 내용");
        request.setIsSecret(true);

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(existingQuestion));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        questionService.updateQuestion(questionId, request, 1L);

        // Then
        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(captor.capture());

        Question updated = captor.getValue();
        assertThat(updated.getQuestionTitle()).isEqualTo("수정된 제목");
        assertThat(updated.getQuestionContent()).isEqualTo("수정된 내용");
        assertThat(updated.getIsSecret()).isTrue();
    }

    @Test
    void updateQuestion_타인문의수정시도_예외발생() {
        // Given
        Long questionId = 1L;
        Long ownerId = 1L;
        Long hackerId = 999L;

        User owner = User.builder().userId(ownerId).build();
        Question existingQuestion = Question.builder()
                .questionId(questionId)
                .user(owner)
                .questionTitle("원래 제목")
                .build();

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(existingQuestion));

        QuestionUpdateRequest request = new QuestionUpdateRequest();
        request.setQuestionTitle("해킹 시도");

        // When & Then
        assertThatThrownBy(() -> questionService.updateQuestion(questionId, request, hackerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인이 작성한 문의만 수정할 수 있습니다.");

        verify(questionRepository, never()).save(any());
    }

    @Test
    void deleteQuestion_본인문의삭제_성공() {
        // Given
        Long questionId = 1L;
        Question question = Question.builder()
                .questionId(questionId)
                .user(user)
                .questionTitle("삭제할 문의")
                .build();

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

        // When
        questionService.deleteQuestion(questionId, 1L);

        // Then
        verify(questionRepository).delete(question);
    }

    @Test
    void deleteQuestion_타인문의삭제시도_예외발생() {
        // Given
        Long questionId = 1L;
        Long ownerId = 1L;
        Long hackerId = 999L;

        User owner = User.builder().userId(ownerId).build();
        Question question = Question.builder()
                .questionId(questionId)
                .user(owner)
                .build();

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

        // When & Then
        assertThatThrownBy(() -> questionService.deleteQuestion(questionId, hackerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인이 작성한 질문만 삭제할 수 있습니다.");

        verify(questionRepository, never()).delete(any());
    }

    @Test
    void getQuestionDetail_본인문의조회_성공() {
        // Given
        Long questionId = 1L;
        Question question = Question.builder()
                .questionId(questionId)
                .user(user)
                .product(product)
                .questionTitle("문의 제목")
                .questionContent("문의 내용")
                .build();

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

        // When
        QuestionManageResponse response = questionService.getQuestionDetail(questionId, 1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getQuestionTitle()).isEqualTo("문의 제목");
        assertThat(response.getQuestionContent()).isEqualTo("문의 내용");
    }

    @Test
    void getQuestionDetail_타인문의조회시도_예외발생() {
        // Given
        Long questionId = 1L;
        Long ownerId = 1L;
        Long otherId = 999L;

        User owner = User.builder().userId(ownerId).build();
        Question question = Question.builder()
                .questionId(questionId)
                .user(owner)
                .build();

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

        // When & Then
        assertThatThrownBy(() -> questionService.getQuestionDetail(questionId, otherId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인이 작성한 문의만 조회할 수 있습니다.");
    }

    @Test
    void getQuestionsByUserId_페이징조회_성공() {
        // Given
        Question question1 = Question.builder()
                .questionId(1L)
                .user(user)
                .questionTitle("문의 1")
                .build();

        Question question2 = Question.builder()
                .questionId(2L)
                .user(user)
                .questionTitle("문의 2")
                .build();

        List<Question> questions = List.of(question1, question2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Question> questionPage = new PageImpl<>(questions, pageable, 2);

        when(questionRepository.findAllByUserIdWithUser(1L, pageable)).thenReturn(questionPage);

        // When
        Page<QuestionManageResponse> result = questionService.getQuestionsByUserId(1L, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getQuestionTitle()).isEqualTo("문의 1");
        assertThat(result.getContent().get(1).getQuestionTitle()).isEqualTo("문의 2");
    }
}
