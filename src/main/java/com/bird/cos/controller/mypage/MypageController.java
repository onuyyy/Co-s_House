package com.bird.cos.controller.mypage;

import com.bird.cos.dto.mypage.MypageUserManageResponse;
import com.bird.cos.dto.mypage.MypageUserUpdateRequest;
import com.bird.cos.service.mypage.MypageService;
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
@RequestMapping("/mypage")
@Slf4j
public class MypageController {

    private final MypageService mypageService;


    /**
     * 마이페이지 홈
     */
    @GetMapping("")
    public String mypage(Model model, Authentication authentication) {
        Long userId = mypageService.getUserIdFromAuthentication(authentication);

        MypageUserManageResponse userInfo = mypageService.getUserInfoById(userId);
        model.addAttribute("userInfo", userInfo);

        // 임시 더미 데이터로 페이지 확인
        model.addAttribute("orderCount", 5);
        model.addAttribute("wishlistCount", 12);
        model.addAttribute("reviewCount", 3);
        model.addAttribute("questionCount", 2);

        return "/mypage/mypage";
    }

    /**
     * 유저 정보 상세보기
     */
    @GetMapping("/mypageUser")
    public String mypageUserDetail(Model model, Authentication authentication) {
        Long userId = mypageService.getUserIdFromAuthentication(authentication);
        MypageUserManageResponse userInfo = mypageService.getUserInfoById(userId);
        model.addAttribute("userInfo", userInfo);
        return "/mypage/mypage-user";
    }

    /**
     * 유저 정보 업데이트
     */
    @PostMapping("/mypageUserUpdate")
    public String mypageUserUpdate(@ModelAttribute MypageUserUpdateRequest rq,
                                   @RequestParam(required = false) String currentPassword,
                                   Authentication authentication) {
        Long userId = mypageService.getUserIdFromAuthentication(authentication);

        mypageService.updateUserInfoById(userId, rq, currentPassword);
        return "redirect:/mypage/mypageUser";
    }

    /**
     * 비밀번호 인증(비밀번호 변경)
     */
    @PostMapping("/validatePassword")
    @ResponseBody
    public ResponseEntity<Boolean> validateCurrentPassword(@RequestParam String currentPassword,
                                                           Authentication authentication,
                                                           HttpServletRequest request) {
        boolean isValid = mypageService.validateCurrentPassword(currentPassword, authentication, request);
        return ResponseEntity.ok(isValid);
    }


    /**
     * 유저 정보 삭제
     */
    @PostMapping("/mypageUserDelete")
    public String mypageUserDelete(Authentication authentication, HttpServletRequest request) {
        try {
            Long userId = mypageService.getUserIdFromAuthentication(authentication);
            log.info("회원 탈퇴가 시작되었습니다. 사용자 ID: {}", userId);

            mypageService.deleteUserInfoById(userId);

            request.getSession().invalidate();
            log.info("회원 탈퇴가 완료되었습니다. 사용자 ID: {}", userId);
            return "redirect:/";

        } catch (Exception e) {
            log.error("회원 탈퇴 처리 중 오류가 발생했습니다: {}", e.getMessage(), e);
            return "redirect:/mypage/mypageUser?error=withdrawal_failed";
        }
    }

}