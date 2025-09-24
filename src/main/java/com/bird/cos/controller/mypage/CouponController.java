package com.bird.cos.controller.mypage;

import com.bird.cos.domain.coupon.CouponScope;
import com.bird.cos.dto.mypage.CouponResponse;
import com.bird.cos.dto.mypage.UserCouponResponse;
import com.bird.cos.service.mypage.CouponService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage/coupons")
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public String couponList(@RequestParam(value = "scope", required = false) CouponScope scope,
                             @RequestParam(value = "brandId", required = false) Long brandId,
                             @RequestParam(value = "status", required = false) String status,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             @PageableDefault(size = 8) Pageable pageable,
                             HttpSession session,
                             Model model) {

        Long userId = (Long) session.getAttribute("userId");

        Page<CouponResponse> couponPage = couponService.searchCoupons(userId, scope, brandId, status, keyword, pageable);

        model.addAttribute("coupons", couponPage.getContent());
        model.addAttribute("page", couponPage);
        model.addAttribute("selectedScope", scope != null ? scope.name() : "");
        model.addAttribute("selectedBrandId", brandId);
        model.addAttribute("selectedStatus", status != null ? status : "");
        model.addAttribute("keyword", keyword != null ? keyword : "");
        return "mypage/coupon-list";
    }

    @PostMapping("/{couponId}/claim")
    public String claimCoupon(@PathVariable Long couponId,
                              HttpSession session,
                              @RequestParam(value = "page", required = false) Integer pageParam,
                              @RequestParam(value = "size", required = false) Integer sizeParam,
                              @RequestParam(value = "scope", required = false) String scopeParam,
                              @RequestParam(value = "brandId", required = false) Long brandIdParam,
                              @RequestParam(value = "status", required = false) String statusParam,
                              @RequestParam(value = "keyword", required = false) String keywordParam,
                              RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/controller/register/login";
        }

        try {
            couponService.claimCoupon(userId, couponId);
            redirectAttributes.addFlashAttribute("success", "쿠폰이 발급되었습니다.");
            addPagingAttributes(redirectAttributes, pageParam, sizeParam, scopeParam, brandIdParam, statusParam, keywordParam);
            return "redirect:/mypage/coupons";
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            addPagingAttributes(redirectAttributes, pageParam, sizeParam, scopeParam, brandIdParam, statusParam, keywordParam);
            return "redirect:/mypage/coupons";
        }
    }

    @GetMapping("/mine")
    public String myCoupons(@PageableDefault(size = 8) Pageable pageable,
                            HttpSession session,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/controller/register/login";
        }

        Page<UserCouponResponse> couponPage = couponService.findUserCoupons(userId, pageable);
        model.addAttribute("userCoupons", couponPage.getContent());
        model.addAttribute("page", couponPage);
        return "mypage/my-coupons";
    }

    private void addPagingAttributes(RedirectAttributes redirectAttributes,
                                     Integer pageParam,
                                     Integer sizeParam,
                                     String scopeParam,
                                     Long brandIdParam,
                                     String statusParam,
                                     String keywordParam) {
        if (pageParam != null) {
            redirectAttributes.addAttribute("page", pageParam);
        }
        if (sizeParam != null) {
            redirectAttributes.addAttribute("size", sizeParam);
        }
        if (scopeParam != null && !scopeParam.isBlank()) {
            redirectAttributes.addAttribute("scope", scopeParam);
        }
        if (brandIdParam != null) {
            redirectAttributes.addAttribute("brandId", brandIdParam);
        }
        if (statusParam != null && !statusParam.isBlank()) {
            redirectAttributes.addAttribute("status", statusParam);
        }
        if (keywordParam != null && !keywordParam.isBlank()) {
            redirectAttributes.addAttribute("keyword", keywordParam);
        }
    }
}
