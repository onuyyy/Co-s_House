package com.bird.cos.controller.question;

import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.domain.product.Question;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.question.QuestionManageResponse;
import com.bird.cos.dto.question.QuestionUpdateRequest;
import com.bird.cos.repository.common.CommonCodeRepository;
import com.bird.cos.service.question.QuestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuestionController.class)
@DisplayName("QuestionController 테스트")
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuestionService questionService;

    @MockitoBean
    private CommonCodeRepository commonCodeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private CommonCode questionType;
    private CommonCode questionStatus;
    private Question testQuestion;
    private QuestionUpdateRequest testQuestionDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .userEmail("test@example.com")
                .userName("테스트사용자")
                .userNickname("테스트닉네임")
                .userPhone("010-1234-5678")
                .userAddress("서울시")
                .userPassword("password")
                .termsAgreed(true)
                .build();

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
    @WithMockUser(username = "test@example.com")
    @DisplayName("문의 목록을 성공적으로 조회한다")
    void questionList_성공() throws Exception {
        // Given
        List<QuestionManageResponse> responses = Arrays.asList(
                QuestionManageResponse.from(testQuestion)
        );
        Page<QuestionManageResponse> questionPage = new PageImpl<>(responses, PageRequest.of(0, 5), 1);

        given(questionService.getUserIdFromAuthentication(any())).willReturn(1L);
        given(questionService.getQuestionsByUserId(eq(1L), any())).willReturn(questionPage);

        // When & Then
        mockMvc.perform(get("/question")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("question/questionList"))
                .andExpect(model().attributeExists("questions"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1));

        verify(questionService, times(1)).getUserIdFromAuthentication(any());
        verify(questionService, times(1)).getQuestionsByUserId(eq(1L), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("문의 작성 폼을 성공적으로 조회한다")
    void selectQuestion_성공() throws Exception {
        // Given
        given(questionService.getUserFromAuthentication(any())).willReturn(testUser);

        // When & Then
        mockMvc.perform(get("/question/info"))
                .andExpect(status().isOk())
                .andExpect(view().name("question/questionInfo"))
                .andExpect(model().attributeExists("questionDto"));

        verify(questionService, times(1)).getUserFromAuthentication(any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("문의글을 성공적으로 등록한다")
    void submitQuestion_성공() throws Exception {
        // Given
        given(questionService.getUserIdFromAuthentication(any())).willReturn(1L);
        given(questionService.saveQuestion(any(QuestionUpdateRequest.class), eq(1L))).willReturn(testQuestion);

        // When & Then
        mockMvc.perform(post("/question/submit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("questionTitle", "테스트 문의")
                        .param("questionContent", "테스트 문의 내용")
                        .param("questionType", "QT_001")
                        .param("isSecret", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/question"));

        verify(questionService, times(1)).getUserIdFromAuthentication(any());
        verify(questionService, times(1)).saveQuestion(any(QuestionUpdateRequest.class), eq(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("문의글 상세보기를 성공적으로 조회한다")
    void selectQuestionDetail_성공() throws Exception {
        // Given
        QuestionManageResponse response = QuestionManageResponse.from(testQuestion);
        given(questionService.getUserIdFromAuthentication(any())).willReturn(1L);
        given(questionService.getQuestionDetail(1L, 1L)).willReturn(response);

        // When & Then
        mockMvc.perform(get("/question/detail/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("question/questionDetail"))
                .andExpect(model().attributeExists("question"));

        verify(questionService, times(1)).getUserIdFromAuthentication(any());
        verify(questionService, times(1)).getQuestionDetail(1L, 1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("문의글 수정 폼을 성공적으로 조회한다")
    void editQuestionForm_성공() throws Exception {
        // Given
        QuestionManageResponse response = QuestionManageResponse.from(testQuestion);
        given(questionService.getUserIdFromAuthentication(any())).willReturn(1L);
        given(questionService.getQuestionDetail(1L, 1L)).willReturn(response);

        // When & Then
        mockMvc.perform(get("/question/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("question/questionInfo"))
                .andExpect(model().attributeExists("questionDto"));

        verify(questionService, times(1)).getUserIdFromAuthentication(any());
        verify(questionService, times(1)).getQuestionDetail(1L, 1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("문의글을 성공적으로 수정한다")
    void updateQuestion_성공() throws Exception {
        // Given
        given(questionService.getUserIdFromAuthentication(any())).willReturn(1L);
        doNothing().when(questionService).updateQuestion(eq(1L), any(QuestionUpdateRequest.class), eq(1L));

        // When & Then
        mockMvc.perform(post("/question/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("questionTitle", "수정된 제목")
                        .param("questionContent", "수정된 내용"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/question"));

        verify(questionService, times(1)).getUserIdFromAuthentication(any());
        verify(questionService, times(1)).updateQuestion(eq(1L), any(QuestionUpdateRequest.class), eq(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("문의글을 성공적으로 삭제한다")
    void deleteQuestion_성공() throws Exception {
        // Given
        given(questionService.getUserIdFromAuthentication(any())).willReturn(1L);
        doNothing().when(questionService).deleteQuestion(1L, 1L);

        // When & Then
        mockMvc.perform(post("/question/1/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/question"));

        verify(questionService, times(1)).getUserIdFromAuthentication(any());
        verify(questionService, times(1)).deleteQuestion(1L, 1L);
    }
}
