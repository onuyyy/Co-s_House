package com.bird.cos.controller.mypage;

import com.bird.cos.dto.mypage.MyLikedProductResponse;
import com.bird.cos.security.CustomUserDetails;
import com.bird.cos.service.auth.AuthService;
import com.bird.cos.service.product.ProductLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Controller
@RequestMapping("/mypage/likes")
@RequiredArgsConstructor
public class ProductLikePageController {

    private final ProductLikeService productLikeService;
    private final AuthService authService;

    @GetMapping
    public String getLikedProductsPage(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @PageableDefault(size = 12) Pageable pageable,
                                       Model model) {
        Page<MyLikedProductResponse> likedProducts = productLikeService.getLikedProducts(userDetails.getUserId(), pageable);

        model.addAttribute("likedProducts", likedProducts);
        model.addAttribute("user", authService.getUser(userDetails.getUserId()));
        return "mypage/my-likes";
    }
}
