package com.bird.cos.controller.order;

import com.bird.cos.dto.order.OrderForm;
import com.bird.cos.dto.order.OrderRequest;
import com.bird.cos.dto.order.OrderResponse;
import com.bird.cos.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/order")
@Controller
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public String createOrder(@ModelAttribute OrderForm orderForm, Model model) {

        // Spring MVC가 @ModelAttribute로 List 자체를 바인딩하는 것은 직접적으로 지원하지
        // 않아서 wrapper dto 로 한번 감싼다
        List<OrderRequest> orderItems = orderForm.getOrderItems();

        OrderResponse order = orderService.createOrder(orderItems);
        model.addAttribute("order", order);

        return "order/create";
    }

}