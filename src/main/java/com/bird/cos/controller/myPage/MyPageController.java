package com.bird.cos.controller.mypage;

import com.bird.cos.dto.mypage.MyPageUserManageResponse;
import com.bird.cos.dto.mypage.MyPageUserUpdateRequest;
import com.bird.cos.service.mypage.MyPageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@Controller
@RequiredArgsConstructor
@RequestMapping("/myPage")
@Slf4j
public class MyPageController {

    private final MyPageService myPageService;


    /**
     * 마이페이지 홈
     */
    @GetMapping("")
    public String myPage(Model model, Authentication authentication) {
        Long userId = myPageService.getUserIdFromAuthentication(authentication);

        MyPageUserManageResponse userInfo = myPageService.getUserInfoById(userId);
        model.addAttribute("userInfo", userInfo);

        // 임시 더미 데이터로 페이지 확인
        model.addAttribute("orderCount", 5);
        model.addAttribute("wishlistCount", 12);
        model.addAttribute("reviewCount", 3);
        model.addAttribute("questionCount", 2);

        return "myPage/myPage";
    }

    /**
     * 유저 정보 상세보기
     */
    @GetMapping("/myPageUserDetail")
    public String myPageUserDetail(Model model, Authentication authentication) {
        Long userId = myPageService.getUserIdFromAuthentication(authentication);
        MyPageUserManageResponse userInfo = myPageService.getUserInfoById(userId);
        model.addAttribute("userInfo", userInfo);
        return "myPage/myPageUserUpdate";
    }

    /**
     * 유저 정보 업데이트
     */
    @PostMapping("/myPageUserUpdate")
    public String myPageUserUpdate(@ModelAttribute MyPageUserUpdateRequest rq,
                                 @RequestParam(required = false) String currentPassword,
                                 Authentication authentication) {
        Long userId = myPageService.getUserIdFromAuthentication(authentication);

        myPageService.updateUserInfoById(userId, rq, currentPassword);
        return "redirect:/myPage/myPageUserDetail";
    }

    /**
     * 비밀번호 인증(비밀번호 변경)
     */
    @PostMapping("/validatePassword")
    @ResponseBody
    public ResponseEntity<Boolean> validateCurrentPassword(@RequestParam String currentPassword,
                                                          Authentication authentication,
                                                          HttpServletRequest request) {
        boolean isValid = myPageService.validateCurrentPassword(currentPassword, authentication, request);
        return ResponseEntity.ok(isValid);
    }


    /**
     * 유저 정보 삭제
     */
    @PostMapping("/myPageUserDelete")
    public String deleteUser(Authentication authentication, HttpServletRequest request) {
        try {
            Long userId = myPageService.getUserIdFromAuthentication(authentication);
            log.info("회원 탈퇴가 시작되었습니다. 사용자 ID: {}", userId);

            myPageService.deleteUserInfoById(userId);

            request.getSession().invalidate();
            log.info("회원 탈퇴가 완료되었습니다. 사용자 ID: {}", userId);
            return "redirect:/";
        } catch (Exception e) {
            log.error("회원 탈퇴 처리 중 오류가 발생했습니다: {}", e.getMessage(), e);
            return "redirect:/myPage/myPageUserDetail?error=withdrawal_failed";
        }
    }

}