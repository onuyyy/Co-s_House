package com.bird.cos.controller.question;

import com.bird.cos.domain.proudct.Question;
import com.bird.cos.domain.user.User;
import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.dto.question.QuestionUpdateRequest;
import com.bird.cos.dto.question.QuestionManageResponse;
import java.util.stream.Collectors;
import static org.mockito.Mockito.doNothing;
import com.bird.cos.service.question.QuestionService;
import com.bird.cos.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuestionController.class)
@DisplayName("QuestionController 테스트")
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestionService questionService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private com.bird.cos.repository.CommonCodeRepository commonCodeRepository;

    @Autowired
    private ObjectMapper objectMapper;

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
    }

    @Test
    @DisplayName("문의 목록 페이지를 성공적으로 조회한다 (페이징)")
    void questionList_페이징_성공() throws Exception {
        // Given
        List<Question> questions = Arrays.asList(testQuestion);
        List<QuestionManageResponse> responses = questions.stream()
                .map(QuestionManageResponse::from)
                .collect(Collectors.toList());
        Page<QuestionManageResponse> questionPage = new PageImpl<>(responses, PageRequest.of(0, 10), 1);
        given(questionService.getQuestionsByUserId(anyLong(), any())).willReturn(questionPage);

        // When & Then
        mockMvc.perform(get("/question")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("question/questionList"))
                .andExpect(model().attributeExists("questions"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalPages"))
                .andExpect(model().attributeExists("totalElements"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalElements", 1L));

        verify(questionService, times(1)).getQuestionsByUserId(anyLong(), any());
    }

    @Test
    @DisplayName("문의 작성 폼 페이지를 성공적으로 조회한다 (사용자 정보 미리 채우기)")
    void selectQuestion_사용자정보미리채우기_성공() throws Exception {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/question/info"))
                .andExpect(status().isOk())
                .andExpect(view().name("question/questionInfo"))
                .andExpect(model().attributeExists("questionDto"));

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("문의글을 성공적으로 등록한다")
    void submitQuestion_성공() throws Exception {
        // Given
        given(questionService.saveQuestion(any(QuestionUpdateRequest.class))).willReturn(testQuestion);

        // When & Then
        mockMvc.perform(post("/question/submit")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("questionTitle", "테스트 문의")
                        .param("questionContent", "테스트 문의 내용")
                        .param("questionType", "QT_001")
                        .param("isSecret", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/question"));

        verify(questionService, times(1)).saveQuestion(any(QuestionUpdateRequest.class));
    }

    @Test
    @DisplayName("문의글 등록 실패 시 에러 페이지를 반환한다")
    void submitQuestion_실패() throws Exception {
        // Given
        given(questionService.saveQuestion(any(QuestionUpdateRequest.class)))
                .willThrow(new RuntimeException("저장 실패"));

        // When & Then
        mockMvc.perform(post("/question/submit")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("questionTitle", "테스트 문의")
                        .param("questionContent", "테스트 문의 내용"))
                .andExpect(status().isOk())
                .andExpect(view().name("question/questionInfo"))
                .andExpect(model().attributeExists("error"));

        verify(questionService, times(1)).saveQuestion(any(QuestionUpdateRequest.class));
    }

    @Test
    @DisplayName("문의글 상세보기를 성공적으로 조회한다")
    void selectQuestionDetail_성공() throws Exception {
        // Given
        QuestionManageResponse response = QuestionManageResponse.from(testQuestion);
        given(questionService.getQuestionDetail(1L)).willReturn(response);

        // When & Then
        mockMvc.perform(get("/question/detail/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("question/questionDetail"))
                .andExpect(model().attributeExists("question"))
                .andExpect(model().attribute("question", response));

        verify(questionService, times(1)).getQuestionDetail(1L);
    }

    @Test
    @DisplayName("존재하지 않는 문의글 조회 시 목록으로 리다이렉트한다")
    void selectQuestionDetail_문의글없음_리다이렉트() throws Exception {
        // Given
        given(questionService.getQuestionDetail(999L)).willThrow(new RuntimeException("문의를 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/question/detail/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/question"));

        verify(questionService, times(1)).getQuestionDetail(999L);
    }

    @Test
    @DisplayName("문의글 수정 폼을 성공적으로 조회한다")
    void editQuestionForm_성공() throws Exception {
        // Given
        QuestionManageResponse response = QuestionManageResponse.from(testQuestion);
        given(questionService.getQuestionDetail(1L)).willReturn(response);

        // When & Then
        mockMvc.perform(get("/question/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("question/questionInfo"))
                .andExpect(model().attributeExists("questionDto"));

        verify(questionService, times(1)).getQuestionDetail(1L);
    }

    @Test
    @DisplayName("존재하지 않는 문의글 수정 시 목록으로 리다이렉트한다")
    void editQuestionForm_문의글없음_리다이렉트() throws Exception {
        // Given
        given(questionService.getQuestionDetail(999L)).willThrow(new RuntimeException("문의를 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/question/999/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/question"));

        verify(questionService, times(1)).getQuestionDetail(999L);
    }

    @Test
    @DisplayName("문의글을 성공적으로 수정한다")
    void updateQuestion_성공() throws Exception {
        // Given
        doNothing().when(questionService).updateQuestion(anyLong(), any(QuestionUpdateRequest.class));

        // When & Then
        mockMvc.perform(post("/question/1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("questionTitle", "수정된 제목")
                        .param("questionContent", "수정된 내용"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/question"));

        verify(questionService, times(1)).updateQuestion(anyLong(), any(QuestionUpdateRequest.class));
    }

    @Test
    @DisplayName("문의글을 성공적으로 삭제한다")
    void deleteQuestion_성공() throws Exception {
        // When & Then
        mockMvc.perform(post("/question/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/question"));

        verify(questionService, times(1)).deleteQuestion(1L);
    }
}