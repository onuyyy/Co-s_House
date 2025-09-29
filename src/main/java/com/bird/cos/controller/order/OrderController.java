package com.bird.cos.controller.order;

import com.bird.cos.dto.order.*;
import com.bird.cos.security.CustomUserDetails;
import com.bird.cos.service.mypage.CouponService;
import com.bird.cos.service.order.OrderService;
import com.bird.cos.service.user.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/order")
@Controller
public class OrderController {

    private final OrderService orderService;
    private final CouponService couponService;
    private final PointService pointService;

    /**
     * 주문 미리보기 - 장바구니에서 구매할 상품들을 선택하고 "주문하기" 버튼을 클릭했을 때
     * DB에 저장하지 않고 주문 정보를 미리보기만 제공
     */
    @PostMapping("/preview")
    public String getOrderView(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                              @ModelAttribute OrderForm orderForm, Model model) {

        List<OrderRequest> orderItems = extractOrderItems(orderForm);

        OrderPreviewResponse orderPreview = orderService.getOrderPreview(customUserDetails.getUserEmail(), orderItems);
        model.addAttribute("order", orderPreview);

        return "order/create";
    }

    /**
     * 실제 주문 생성 - 주문 미리보기 화면에서 "결제하기" 버튼을 클릭했을 때
     * DB에 주문과 주문상품을 실제로 저장하고 성공 응답만 반환
     */
    @PostMapping("/create")
    @ResponseBody
    public OrderCreateResponse createOrder(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                          @ModelAttribute OrderForm orderForm) {
        try {
            List<OrderRequest> orderItems = extractOrderItems(orderForm);

            OrderResponse order = orderService.createOrder(
                customUserDetails.getUserEmail(),
                orderItems,
                orderForm.getUserCouponId(),
                orderForm.getCouponDiscountAmount(),
                orderForm.getUsedPoints(),
                orderForm.getFinalAmount()
            );

            return OrderCreateResponse.success(order.getOrderId());

        } catch (Exception e) {
            return OrderCreateResponse.failure("주문 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * OrderForm에서 OrderRequest 리스트를 추출하는 공통 메서드
     */
    private List<OrderRequest> extractOrderItems(OrderForm orderForm) {
        // Spring MVC가 @ModelAttribute로 List 자체를 바인딩하는 것은 직접적으로 지원하지
        // 않아서 wrapper dto로 한번 감싼다
        return orderForm.getOrderItems();
    }

    @GetMapping("/my-coupons")
    @ResponseBody
    public List<MyCouponResponse> getMyCoupons(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                              @RequestParam(required = false) List<Long> productIds)
    {
        if (productIds != null && !productIds.isEmpty()) {
            // 주문 상품에 적용 가능한 쿠폰만 필터링 (CouponScope 고려)
            return couponService.getApplicableCoupons(customUserDetails.getUserId(), productIds);
        } else {
            // 상품 정보가 없으면 모든 쿠폰 반환
            return couponService.getMyCoupons(customUserDetails.getUserId());
        }
    }

    /**
     * 쿠폰 적용 가능 금액 확인
     * @param userCouponId 사용자 쿠폰 ID
     * @param orderAmount 주문 금액 (배송비 포함)
     * @return 쿠폰 적용 결과
     */
    @GetMapping("/my-coupons/{userCouponId}")
    @ResponseBody
    public SalesPriceCheckResponse checkMyCoupon(@PathVariable String userCouponId, @RequestParam BigDecimal orderAmount) {
        return couponService.checkUserCoupon(Long.valueOf(userCouponId), orderAmount);
    }

    /**
     * 사용자의 현재 포인트 조회
     * @param customUserDetails 인증된 사용자 정보
     * @return 포인트 정보 응답
     */
    @GetMapping("/my-points")
    @ResponseBody
    public Map<String, Object> getMyPoints(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            Integer availablePoints = pointService.getAvailablePoints(customUserDetails.getUserId());

            response.put("success", true);
            response.put("totalPoints", availablePoints != null ? availablePoints : 0);
            response.put("message", "포인트 조회 성공");

        } catch (Exception e) {
            response.put("success", false);
            response.put("totalPoints", 0);
            response.put("message", "포인트 조회 실패: " + e.getMessage());
        }

        return response;
    }

}