package com.bird.cos.controller.mypage;

import com.bird.cos.dto.mypage.MyOrderRequest;
import com.bird.cos.dto.mypage.MypageUserManageResponse;
import com.bird.cos.dto.mypage.MypageUserUpdateRequest;
import com.bird.cos.dto.order.MyOrderResponse;
import com.bird.cos.security.CustomUserDetails;
import com.bird.cos.service.event.EventService;
import com.bird.cos.service.mypage.CouponService;
import com.bird.cos.service.mypage.MypageService;
import com.bird.cos.service.order.OrderService;
import com.bird.cos.service.user.PointService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
@Slf4j
public class MypageController {

    private final MypageService mypageService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final PointService pointService;
    private final EventService eventService;

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

    /**
     * 주문 내역 조회
     */
    @RequestMapping(value = "/my-orders", method = {RequestMethod.GET, RequestMethod.POST})
    public String mypageOrderList(@AuthenticationPrincipal CustomUserDetails user,
                                   @ModelAttribute MyOrderRequest request,
                                   Model model,
                                   @PageableDefault(size = 10) Pageable pageable) {
        
        Page<MyOrderResponse> orderPage = orderService.getMyOrders(user.getUserId(), request, pageable);
        
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("searchRequest", request != null ? request : new MyOrderRequest());

        // 쿠폰 개수 / 포인트 가져오기
        long myCouponCnt = couponService.getMyCouponsCount(user.getUserId());
        Integer myPointCnt = Optional.ofNullable(pointService.getAvailablePoints(user.getUserId())).orElse(0);

        model.addAttribute("myCouponCount", myCouponCnt);
        model.addAttribute("myPointCount", myPointCnt);

        // 주문 상태별 카운트 (현재 페이지 기준)
        List<MyOrderResponse> allOrders = orderPage.getContent();
        long pendingCount = allOrders.stream().filter(o -> "ORDER_001".equals(o.getOrderStatusCode())).count();
        long paidCount = allOrders.stream().filter(o -> "ORDER_002".equals(o.getOrderStatusCode())).count();
        long preparingCount = allOrders.stream().filter(o -> "ORDER_003".equals(o.getOrderStatusCode())).count();
        long shippingCount = allOrders.stream().filter(o -> "ORDER_004".equals(o.getOrderStatusCode())).count();
        long deliveredCount = allOrders.stream().filter(o -> "ORDER_005".equals(o.getOrderStatusCode())).count();
        long confirmedCount = allOrders.stream().filter(o -> "ORDER_006".equals(o.getOrderStatusCode())).count();

        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("paidCount", paidCount);
        model.addAttribute("preparingCount", preparingCount);
        model.addAttribute("shippingCount", shippingCount);
        model.addAttribute("deliveredCount", deliveredCount);
        model.addAttribute("confirmedCount", confirmedCount);

        return "mypage/order-list";
    }

    @GetMapping("/points")
    public String mypagePointPage() {



        return  "mypage/points";
    }

}