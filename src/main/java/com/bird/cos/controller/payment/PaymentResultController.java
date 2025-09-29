package com.bird.cos.controller.payment;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentResultController {

    @GetMapping("/payment/success")
    public String success(@RequestParam(required = false) String orderId,
                          @RequestParam(required = false) String paymentKey,
                          Model model) {
        model.addAttribute("orderId", orderId);
        model.addAttribute("paymentKey", paymentKey);
        return "payment/success";
    }

    @GetMapping("/payment/fail")
    public String fail(@RequestParam(required = false) String message,
                       @RequestParam(required = false, name = "code") String errorCode,
                       Model model) {
        model.addAttribute("errorCode", errorCode);
        model.addAttribute("message", message);
        return "payment/fail";
    }
}

