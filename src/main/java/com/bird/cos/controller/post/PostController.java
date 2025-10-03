package com.bird.cos.controller.post;

import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.dto.mypage.MyOrderRequest;
import com.bird.cos.dto.order.OrderItemResponse;
import com.bird.cos.dto.post.PostDetailResponse;
import com.bird.cos.dto.post.PostRequest;
import com.bird.cos.dto.post.PostResponse;
import com.bird.cos.dto.post.PostSearchRequest;
import com.bird.cos.dto.product.ProductResponse;
import com.bird.cos.security.CustomUserDetails;
import com.bird.cos.service.admin.common.CommonCodeService;
import com.bird.cos.service.order.OrderItemService;
import com.bird.cos.service.order.OrderService;
import com.bird.cos.service.post.PostService;
import com.bird.cos.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/posts")
@Controller
public class PostController {

    private final PostService postService;
    private final CommonCodeService commonCodeService;
    private final OrderItemService orderItemService;

    @GetMapping
    public String getPostPage(PostSearchRequest searchRequest,
                              Model model,
                              @PageableDefault(size = 12, sort = "postCreatedAt", direction = Sort.Direction.DESC) Pageable pageable)
    {
        
        Page<PostResponse> posts = postService.getPosts(searchRequest, pageable);

        model.addAttribute("posts", posts);
        model.addAttribute("searchRequest", searchRequest);

        return "posts/index";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/new")
    public String getNewPostPage(Model model)
    {
        List<CommonCode> postInformation = commonCodeService.getCommonCodeList("POST_INFORMATION");
        model.addAttribute("postInformation", postInformation);

        return "posts/new";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/new")
    public String submitNewPost(
            @ModelAttribute PostRequest postRequest,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model)
    {
        try {
            // 이미지 파일을 PostRequest에 설정
            if (images != null && !images.isEmpty()) {
                postRequest.setImages(images);
            }

            // 게시글 저장 (이미지 포함)
            postService.createPost(postRequest, userDetails.getUserId());

            return "redirect:/posts";
        } catch (Exception e) {
            model.addAttribute("error", "게시글 작성 중 오류가 발생했습니다: " + e.getMessage());
            
            // 실패 시 다시 작성 페이지로
            List<CommonCode> postInformation = commonCodeService.getCommonCodeList("POST_INFORMATION");
            model.addAttribute("postInformation", postInformation);
            
            return "posts/new";
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/products")
    @ResponseBody
    public ResponseEntity<List<OrderItemResponse>> getProducts(Model model,
                                                             @ModelAttribute MyOrderRequest request,
                                                             @PageableDefault Pageable pageable,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails)
    {
        List<OrderItemResponse> myOrderItems = orderItemService.getMyOrderItems(userDetails.getUserId());
        return ResponseEntity.ok(myOrderItems);
    }

    @GetMapping("/{postId}")
    public String getPostDetail(@PathVariable long postId, Model model)
    {
        PostDetailResponse post = postService.getPost(postId);
        model.addAttribute("post", post);

        return "posts/detail";
    }
}
