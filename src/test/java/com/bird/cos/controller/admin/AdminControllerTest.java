package com.bird.cos.controller.admin;

import com.bird.cos.dto.admin.AdminUserResponse;
import com.bird.cos.dto.admin.AdminUserSearchType;
import com.bird.cos.service.admin.AdminService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
        mockMvc.perform(get("/admin/main"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/admin/user/")); // Thymeleaf용 forward 확인
    }

    @DisplayName("[Controller] - 사용자 관리 화면 접속")
    @Test
    void givenPaging_whenRequestUserPage_thenUserPage() throws Exception {
        // given
        AdminUserResponse user1 = AdminUserResponse.builder()
                .userNickname("test-nickname")
                .userName("test-user")
                .build();

        AdminUserResponse user2 = AdminUserResponse.builder()
                .userNickname("test-nickname2")
                .userName("test-user2")
                .build();

        List<AdminUserResponse> users = Arrays.asList(user1, user2);
        Page<AdminUserResponse> usersPage = new PageImpl<>(users);

        // when
        Mockito.when(adminService.getUserList(
                eq(AdminUserSearchType.NAME),
                eq("User"),
                any(Pageable.class)
        )).thenReturn(usersPage);

        // then
        mockMvc.perform(get("/admin/user") // 경로에서 searchValue 제거
                        .param("searchType", "NAME")
                        .param("searchValue", "User") // 쿼리 파라미터로 전달
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-list")) // 뷰 이름 확인
                .andExpect(model().attributeExists("userList")) // 모델 속성 존재 확인
                .andExpect(model().attribute("userList", usersPage));
    }
}
