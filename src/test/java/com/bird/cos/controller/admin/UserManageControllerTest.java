package com.bird.cos.controller.admin;

import com.bird.cos.dto.admin.UserManageResponse;
import com.bird.cos.dto.admin.UserManageSearchType;
import com.bird.cos.dto.admin.UserUpdateRequest;
import com.bird.cos.service.admin.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({UserManageController.class, AdminMainController.class})
public class UserManageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminService adminService;

    // 테스트 전용 설정 클래스 -> Spring Test 컨텍스트에서만 사용
    // WebMvcTest는 Controller만 로드
    // Service Bean을 스캔하도록 mock bean을 등록
    @TestConfiguration
    static class TestConfig {
        @Bean
        public AdminService adminService() {
            return Mockito.mock(AdminService.class);
        }
    }

    @DisplayName("[Controller] - 어드민 메인 화면 접속")
    @Test
    void givenNothing_whenRequestRootPage_thenForwardToUser() throws Exception {
        mockMvc.perform(get("/api/admin/main"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/admin/user/")); // Thymeleaf용 forward 확인
    }

    @DisplayName("[Controller] - 사용자 관리 화면 접속")
    @Test
    void givenPaging_whenRequestUserPage_thenUserPage() throws Exception {
        // given
        UserManageResponse user1 = UserManageResponse.builder()
                .userNickname("test-nickname")
                .userName("test-user")
                .build();

        UserManageResponse user2 = UserManageResponse.builder()
                .userNickname("test-nickname2")
                .userName("test-user2")
                .build();

        List<UserManageResponse> users = Arrays.asList(user1, user2);
        Page<UserManageResponse> usersPage = new PageImpl<>(users);

        // when
        Mockito.when(adminService.getUserList(
                eq(UserManageSearchType.NAME),
                eq("User"),
                any(Pageable.class)
        )).thenReturn(usersPage);

        // then
        mockMvc.perform(get("/api/admin/users") // 경로에서 searchValue 제거
                        .param("searchType", "NAME")
                        .param("searchValue", "User") // 쿼리 파라미터로 전달
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-list")) // 뷰 이름 확인
                .andExpect(model().attributeExists("userList")) // 모델 속성 존재 확인
                .andExpect(model().attribute("userList", usersPage));
    }

    @DisplayName("[Controller] - 사용자 관리 상세 화면")
    @Test
    void givenUserId_whenRequestUserPage_thenUserPage() throws Exception {
        // given
        UserManageResponse user = UserManageResponse.builder()
                .userId(1L)
                .userNickname("test-nickname")
                .userName("test-user")
                .userPhone("1111")
                .termsAgreed(true)
                .build();

        // when
        Mockito.when(adminService.getUserDetail(1L)).thenReturn(user);

        // then
        mockMvc.perform(get("/api/admin/users/" + user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-detail"))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attributeExists("user"));

    }

    @DisplayName("[Controller] - 사용자 정보 업데이트")
    @Test
    void givenUserUpdateRequest_whenPostUpdate_thenRedirect() throws Exception {
        // given
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUserEmail("test@example.com");
        request.setUserNickname("test-nickname");
        request.setUserName("test-user");
        request.setUserAddress("Seoul");
        request.setUserPhone("010-1111-2222");
        request.setSocialProvider("GOOGLE");
        request.setSocialId("google123");
        request.setTermsAgreed(true);

        // when & then
        mockMvc.perform(post("/api/admin/users/{userId}/update", userId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("userEmail", "test@example.com")
                        .param("userNickname", "test-nickname")
                        .param("userName", "test-user")
                        .param("userAddress", "Seoul")
                        .param("userPhone", "010-1111-2222")
                        .param("socialProvider", "GOOGLE")
                        .param("socialId", "google123")
                        .param("termsAgreed", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/admin/users/" + userId));

    }

    @Test
    @DisplayName("[Controller] - 사용자 삭제 후 리다이렉트")
    void givenUserId_whenDelete_thenRedirect() throws Exception {
        // given
        Long userId = 1L;

        // when & then
        mockMvc.perform(post("/api/admin/users/{userId}/delete", userId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/admin/users"));

        // 서비스 호출 검증
        Mockito.verify(adminService, times(1)).deleteUser(userId);
    }
}
