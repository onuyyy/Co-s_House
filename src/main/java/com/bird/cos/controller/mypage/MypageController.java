package com.bird.cos.controller.mypage;

import com.bird.cos.dto.mypage.*;
import com.bird.cos.dto.order.MyOrderResponse;
import com.bird.cos.dto.order.OrderStatusCode;
import com.bird.cos.dto.product.ReviewResponse;
import com.bird.cos.repository.product.ReviewRepository;
import com.bird.cos.security.CustomUserDetails;
import com.bird.cos.service.mypage.CouponService;
import com.bird.cos.service.mypage.MypageService;
import com.bird.cos.service.order.OrderService;
import com.bird.cos.service.product.ProductLikeService;
import com.bird.cos.service.scrap.ScrapService;
import com.bird.cos.service.user.PointService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
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
    private final ReviewRepository reviewRepository;
    private final ScrapService scrapService;
    private final ProductLikeService productLikeService;


    /**
     * 마이페이지 홈
     */
    @GetMapping("")
    public String mypage(Model model, Authentication authentication) {
        Long userId = mypageService.getUserIdFromAuthentication(authentication);

        com.bird.cos.dto.mypage.MypageUserManageResponse userInfo = mypageService.getUserInfoById(userId);
        model.addAttribute("userInfo", userInfo);

        long reviewCount = reviewRepository.countByUserNickname(userInfo.getUserNickname());
        long paidOrderCount = mypageService.countOrdersByStatus(userId, OrderStatusCode.PAID);
        long wishlistCount = productLikeService.countLikedProducts(userId);
        long scrapCount = scrapService.countByUser(userId);
        long questionCount = mypageService.countQuestions(userId);

        model.addAttribute("orderCount", paidOrderCount);
        model.addAttribute("paidOrderCount", paidOrderCount);
        model.addAttribute("wishlistCount", wishlistCount);
        model.addAttribute("scrapCount", scrapCount);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("questionCount", questionCount);
        model.addAttribute("recentOrders", mypageService.getRecentOrders(userId, 5));
        model.addAttribute("recentQuestions", mypageService.getRecentQuestions(userId, 5));

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
    public Object mypageUserDelete(@RequestParam String withdrawalReason,
                                   @RequestParam(required = false) String withdrawalPassword,
                                   @RequestParam(defaultValue = "false") boolean withdrawalAgree,
                                   Authentication authentication,
                                   HttpServletRequest request,
                                   RedirectAttributes redirectAttributes) {
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
        try {
            Long userId = mypageService.getUserIdFromAuthentication(authentication);
            log.info("회원 탈퇴가 시작되었습니다. 사용자 ID: {}", userId);

            if (!StringUtils.hasText(withdrawalReason)) {
                if (isAjax) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "탈퇴 사유를 선택해주세요."
                    ));
                }
                redirectAttributes.addFlashAttribute("withdrawalError", "탈퇴 사유를 선택해주세요.");
                redirectAttributes.addFlashAttribute("withdrawalFormOpen", true);
                redirectAttributes.addFlashAttribute("withdrawalReasonValue", withdrawalReason);
                return "redirect:/mypage/mypageUser";
            }

            if (!withdrawalAgree) {
                if (isAjax) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "회원탈퇴 안내사항에 동의해주세요."
                    ));
                }
                redirectAttributes.addFlashAttribute("withdrawalError", "회원탈퇴 안내사항에 동의해주세요.");
                redirectAttributes.addFlashAttribute("withdrawalFormOpen", true);
                redirectAttributes.addFlashAttribute("withdrawalReasonValue", withdrawalReason);
                return "redirect:/mypage/mypageUser";
            }

            boolean passwordRequired = mypageService.requiresPassword(userId);
            if (passwordRequired) {
                if (!StringUtils.hasText(withdrawalPassword)) {
                    if (isAjax) {
                        return ResponseEntity.badRequest().body(Map.of(
                                "success", false,
                                "message", "비밀번호를 입력해주세요."
                        ));
                    }
                    redirectAttributes.addFlashAttribute("withdrawalError", "비밀번호를 입력해주세요.");
                    redirectAttributes.addFlashAttribute("withdrawalFormOpen", true);
                    redirectAttributes.addFlashAttribute("withdrawalReasonValue", withdrawalReason);
                    return "redirect:/mypage/mypageUser";
                }

                boolean passwordValid = mypageService.validateCurrentPassword(withdrawalPassword, authentication, request);
                if (!passwordValid) {
                    if (isAjax) {
                        return ResponseEntity.badRequest().body(Map.of(
                                "success", false,
                                "message", "현재 비밀번호가 일치하지 않습니다."
                        ));
                    }
                    redirectAttributes.addFlashAttribute("withdrawalError", "현재 비밀번호가 일치하지 않습니다.");
                    redirectAttributes.addFlashAttribute("withdrawalFormOpen", true);
                    redirectAttributes.addFlashAttribute("withdrawalReasonValue", withdrawalReason);
                    return "redirect:/mypage/mypageUser";
                }
            }

            mypageService.deleteUserInfoById(userId, withdrawalReason);

            request.getSession().invalidate();
            log.info("회원 탈퇴가 완료되었습니다. 사용자 ID: {}", userId);
            if (isAjax) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "redirectUrl", "/"
                ));
            }
            return "redirect:/";

        } catch (IllegalStateException e) {
            log.warn("회원 탈퇴 불가 - userId: {}, cause: {}", authentication != null ? authentication.getName() : "anonymous", e.getMessage());
            if (isAjax) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", e.getMessage()
                ));
            }
            redirectAttributes.addFlashAttribute("withdrawalError", e.getMessage());
            redirectAttributes.addFlashAttribute("withdrawalFormOpen", true);
            redirectAttributes.addFlashAttribute("withdrawalReasonValue", withdrawalReason);
            return "redirect:/mypage/mypageUser?error=withdrawal_failed";
        } catch (Exception e) {
            log.error("회원 탈퇴 처리 중 오류가 발생했습니다: {}", e.getMessage(), e);
            if (isAjax) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                        "success", false,
                        "message", "회원 탈퇴 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                ));
            }
            redirectAttributes.addFlashAttribute("withdrawalError", "회원 탈퇴 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            redirectAttributes.addFlashAttribute("withdrawalFormOpen", true);
            redirectAttributes.addFlashAttribute("withdrawalReasonValue", withdrawalReason);
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
                                   @PageableDefault(size = 10) Pageable pageable)
    {
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

    /**
     * 적립금 내역 조회
     * @param user 인증된 사용자 정보
     * @param pageable 페이징 정보
     * @param request 검색 조건
     * @param model 뷰에 전달할 데이터
     * @return 적립금 내역 페이지
     */
    @RequestMapping(value = "/points", method =  {RequestMethod.GET, RequestMethod.POST})
    public String mypagePointPage(@AuthenticationPrincipal CustomUserDetails user,
                                  @PageableDefault(size = 10) Pageable pageable,
                                  MyPointRequest request,
                                  Model model)
    {
        log.info("=== 포인트 페이지 조회 시작 - userId: {} ===", user.getUserId());

        // 적립금 내역 조회
        Page<MyPointResponse> pointsPage = pointService.getMyPointsHistory(user.getUserId(), request, pageable);
        log.info("포인트 내역 조회 완료 - 총 {}건", pointsPage.getTotalElements());

        // 포인트 요약 정보 조회
        MyPointSummary pointSummary = pointService.getPointSummary(user.getUserId());
        log.info("포인트 요약 - 현재: {}P, 월적립: {}P, 월사용: {}P",
                pointSummary.getCurrentPoint(), pointSummary.getMonthEarned(), pointSummary.getMonthUsed());

        // Model에 데이터 추가
        model.addAttribute("pointsPage", pointsPage);
        model.addAttribute("currentPoints", pointSummary.getCurrentPoint());
        model.addAttribute("monthlyEarn", pointSummary.getMonthEarned());
        model.addAttribute("monthlyUse", pointSummary.getMonthUsed());
        model.addAttribute("expiringPoints", pointSummary.getExpiringPoint());
        model.addAttribute("searchRequest", request != null ? request : new MyPointRequest());
        model.addAttribute("totalElements", pointsPage.getTotalElements());

        return "mypage/points";
    }

    /**
     * 내가 작성한 리뷰 조회
     */

    @GetMapping("/reviews")
    public String mypageReviews(@AuthenticationPrincipal CustomUserDetails user,
                                @RequestParam(required = false, defaultValue = "latest") String sort,
                                @RequestParam(required = false, defaultValue = "1") int page,
                                @RequestParam(required = false, defaultValue = "10") int size,
                                Model model) {
        try {
            String userNickname = user.getNickname();

            // 해당 사용자가 작성한 리뷰 조회 (페이징 적용)
            Map<String, Object> result = mypageService.getMyReviews(userNickname, sort, page, size);

            List<ReviewResponse> reviews = (List<ReviewResponse>) result.get("reviews");
            int totalPages = (Integer) result.get("totalPages");
            long totalElements = (Long) result.get("totalElements");

            model.addAttribute("reviews", reviews);
            model.addAttribute("totalReviewCount", totalElements);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("currentSort", sort);

            return "mypage/my-reviews";
        } catch (Exception e) {
            log.error("내 리뷰 조회 중 오류 발생", e);
            model.addAttribute("error", "리뷰를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "mypage/my-reviews";
        }
    }
}
