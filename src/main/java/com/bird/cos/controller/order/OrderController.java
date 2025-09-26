package com.bird.cos.controller.order;

import com.bird.cos.dto.order.MyCouponResponse;
import com.bird.cos.dto.order.OrderCreateResponse;
import com.bird.cos.dto.order.OrderForm;
import com.bird.cos.dto.order.OrderPreviewResponse;
import com.bird.cos.dto.order.OrderRequest;
import com.bird.cos.dto.order.OrderResponse;
import com.bird.cos.security.CustomUserDetails;
import com.bird.cos.service.mypage.CouponService;
import com.bird.cos.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/order")
@Controller
public class OrderController {

    private final OrderService orderService;
    private final CouponService couponService;

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
    public List<MyCouponResponse> getMyCoupons() {

        return couponService.getMyCoupons();
    }

}