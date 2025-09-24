package com.bird.cos.controller.myPage;

import com.bird.cos.controller.myPage.MyPageController;
import com.bird.cos.domain.user.User;
import com.bird.cos.domain.user.UserRole;
import com.bird.cos.dto.myPage.MyPageUserManageResponse;
import com.bird.cos.dto.myPage.MyPageUserUpdateRequest;
import com.bird.cos.service.myPage.MyPageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(MyPageController.class)
@DisplayName("MyPageController 테스트")
class MyPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MyPageService myPageService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private MyPageUserManageResponse testUserResponse;
    private MyPageUserUpdateRequest testUpdateRequest;
    private UserRole testUserRole;

    @BeforeEach
    void setUp() {
        testUserRole = new UserRole(); // UserRole에 builder가 없으므로 직접 생성

        testUser = User.builder()
                .userId(1L)
                .userEmail("test@example.com")
                .userName("테스트사용자")
                .userNickname("테스트닉네임")
                .userPhone("010-1234-5678")
                .userAddress("서울시 강남구")
                .userPassword("encodedPassword")
                .userRole(testUserRole)
                .socialProvider(null)
                .userCreatedAt(LocalDateTime.now())
                .termsAgreed(true)
                .build();

        testUserResponse = MyPageUserManageResponse.builder()
                .userName("테스트사용자")
                .userEmail("test@example.com")
                .userNickname("테스트닉네임")
                .userPhone("010-1234-5678")
                .userAddress("서울시 강남구")
                .userRole("ROLE_USER")
                .socialProvider(null)
                .userCreatedAt(LocalDateTime.now())
                .membershipGrade(null)
                .totalOrderAmount(BigDecimal.ZERO)
                .membershipPoints(0)
                .build();

        testUpdateRequest = new MyPageUserUpdateRequest();
        testUpdateRequest.setUserNickname("수정된닉네임");
        testUpdateRequest.setUserPhone("010-9876-5432");
        testUpdateRequest.setUserAddress("서울시 서초구");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("마이페이지 홈을 성공적으로 조회한다")
    void myPage_성공() throws Exception {
        // Given
        given(myPageService.getUserIdFromAuthentication(any())).willReturn(1L);
        given(myPageService.getUserInfoById(1L)).willReturn(testUserResponse);

        // When & Then
        mockMvc.perform(get("/myPage"))
                .andExpect(status().isOk())
                .andExpect(view().name("myPage/myPage"))
                .andExpect(model().attributeExists("userInfo"))
                .andExpect(model().attributeExists("orderCount"))
                .andExpect(model().attributeExists("wishlistCount"))
                .andExpect(model().attributeExists("reviewCount"))
                .andExpect(model().attributeExists("questionCount"));

        verify(myPageService, times(1)).getUserIdFromAuthentication(any());
        verify(myPageService, times(1)).getUserInfoById(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("유저 정보 상세보기를 성공적으로 조회한다")
    void myPageUserDetail_성공() throws Exception {
        // Given
        given(myPageService.getUserIdFromAuthentication(any())).willReturn(1L);
        given(myPageService.getUserInfoById(1L)).willReturn(testUserResponse);

        // When & Then
        mockMvc.perform(get("/myPage/myPageUserDetail"))
                .andExpect(status().isOk())
                .andExpect(view().name("myPage/myPageUserUpdate"))
                .andExpect(model().attributeExists("userInfo"));

        verify(myPageService, times(1)).getUserIdFromAuthentication(any());
        verify(myPageService, times(1)).getUserInfoById(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("유저 정보를 성공적으로 업데이트한다")
    void myPageUserUpdate_성공() throws Exception {
        // Given
        given(myPageService.getUserIdFromAuthentication(any())).willReturn(1L);
        doNothing().when(myPageService).updateUserInfoById(eq(1L), any(MyPageUserUpdateRequest.class), eq("currentPassword"));

        // When & Then
        mockMvc.perform(post("/myPage/myPageUserUpdate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("userNickname", "수정된닉네임")
                        .param("userPhone", "010-9876-5432")
                        .param("userAddress", "서울시 서초구")
                        .param("currentPassword", "currentPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myPage/myPageUserDetail"));

        verify(myPageService, times(1)).getUserIdFromAuthentication(any());
        verify(myPageService, times(1)).updateUserInfoById(eq(1L), any(MyPageUserUpdateRequest.class), eq("currentPassword"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("비밀번호 검증이 성공한다")
    void validatePassword_성공() throws Exception {
        // Given
        given(myPageService.validateCurrentPassword(eq("correctPassword"), any(), any())).willReturn(true);

        // When & Then
        mockMvc.perform(post("/myPage/validatePassword")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("currentPassword", "correctPassword"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(myPageService, times(1)).validateCurrentPassword(eq("correctPassword"), any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("비밀번호 검증이 실패한다")
    void validatePassword_실패() throws Exception {
        // Given
        given(myPageService.validateCurrentPassword(eq("wrongPassword"), any(), any())).willReturn(false);

        // When & Then
        mockMvc.perform(post("/myPage/validatePassword")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("currentPassword", "wrongPassword"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(myPageService, times(1)).validateCurrentPassword(eq("wrongPassword"), any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("소셜 로그인 사용자의 비밀번호 검증이 실패한다")
    void validatePassword_소셜로그인_실패() throws Exception {
        // Given
        given(myPageService.validateCurrentPassword(eq("anyPassword"), any(), any())).willReturn(false);

        // When & Then
        mockMvc.perform(post("/myPage/validatePassword")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("currentPassword", "anyPassword"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(myPageService, times(1)).validateCurrentPassword(eq("anyPassword"), any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("회원탈퇴를 성공적으로 처리한다")
    void deleteUser_성공() throws Exception {
        // Given
        given(myPageService.getUserIdFromAuthentication(any())).willReturn(1L);
        doNothing().when(myPageService).deleteUserInfoById(1L);

        // When & Then
        mockMvc.perform(post("/myPage/myPageUserDelete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(myPageService, times(1)).getUserIdFromAuthentication(any());
        verify(myPageService, times(1)).deleteUserInfoById(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("존재하지 않는 사용자로 인해 회원탈퇴가 실패한다")
    void deleteUser_사용자없음_실패() throws Exception {
        // Given
        given(myPageService.getUserIdFromAuthentication(any())).willThrow(new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(post("/myPage/myPageUserDelete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/myPage/myPageUserDetail?error=withdrawal_failed"));

        verify(myPageService, times(1)).getUserIdFromAuthentication(any());
    }
}