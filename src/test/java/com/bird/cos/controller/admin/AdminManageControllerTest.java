package com.bird.cos.controller.admin;

import com.bird.cos.dto.admin.AdminManageResponse;
import com.bird.cos.dto.admin.AdminManagerSearchType;
import com.bird.cos.dto.admin.AdminUpdateRequest;
import com.bird.cos.dto.admin.UserUpdateRequest;
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
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminManageController.class)
class AdminManageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminService adminService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AdminService adminService() {
            return Mockito.mock(AdminService.class);
        }
    }

    @DisplayName("[Controller] - 어드민 관리 화면 접속")
    @Test
    void givenPaging_whenRequestAdminPage_thenAdminPage() throws Exception {
        // given
        AdminManageResponse admin1 = AdminManageResponse.builder()
                .adminName("admin1")
                .adminPhone("010-4441-9026")
                .build();
        AdminManageResponse admin2 = AdminManageResponse.builder()
                .adminName("admin2")
                .adminPhone("010-4442-9026")
                .build();

        List<AdminManageResponse> admins = Arrays.asList(admin1, admin2);
        Page<AdminManageResponse> adminPage = new PageImpl<>(admins);

        Mockito.when(adminService.getAdminList(
                eq(AdminManagerSearchType.NAME),
                eq("Admin"),
                any(Pageable.class)
        )).thenReturn(adminPage);

        // when & then
        mockMvc.perform(get("/api/admin/admins")
                        .param("searchType", "NAME")
                        .param("searchValue", "Admin")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/admin-list"))
                .andExpect(model().attributeExists("adminList"))
                .andExpect(model().attribute("adminList", adminPage));
    }

    @DisplayName("[Controller] - 관리자 관리 상세 화면")
    @Test
    void givenAdminId_whenRequestAdminDetailPage_thenAdminDetailPage() throws Exception {
        // given
        AdminManageResponse admin = AdminManageResponse.builder()
                .adminId(1L)
                .adminName("test-nickname")
                .adminPhone("1111")
                .build();

        // when
        Mockito.when(adminService.getAdminDetail(1L)).thenReturn(admin);

        // then
        mockMvc.perform(get("/api/admin/admins/" + admin.getAdminId()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/admin-detail"))
                .andExpect(model().attribute("admin", admin))
                .andExpect(model().attributeExists("admin"));

    }

    @DisplayName("[Controller] - 관리자 정보 업데이트")
    @Test
    void givenAdminUpdateRequest_whenPostUpdate_thenRedirect() throws Exception {
        // given
        Long adminId = 1L;
        AdminUpdateRequest request = new AdminUpdateRequest();
        request.setAdminEmail("test@example.com");
        request.setAdminName("test-name");

        // when & then
        mockMvc.perform(post("/api/admin/admins/{adminId}/update", adminId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("adminEmail", "test@example.com")
                        .param("adminName", "test-name"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/admin/admins/" + adminId));

    }

    @Test
    @DisplayName("[Controller] - 관리자 삭제 후 리다이렉트")
    void givenAdminId_whenDelete_thenRedirect() throws Exception {
        // given
        Long adminId = 1L;

        // when & then
        mockMvc.perform(post("/api/admin/admins/{adminId}/delete", adminId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/admin/admins"));

        // 서비스 호출 검증
        Mockito.verify(adminService, times(1)).deleteAdmin(adminId);
    }
}