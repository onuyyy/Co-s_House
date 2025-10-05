package com.bird.cos.controller.post;

import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.dto.mypage.MyOrderRequest;
import com.bird.cos.dto.order.OrderItemResponse;
import com.bird.cos.dto.post.PostDetailResponse;
import com.bird.cos.dto.post.PostRequest;
import com.bird.cos.dto.post.PostResponse;
import com.bird.cos.dto.post.PostSearchRequest;
import com.bird.cos.security.CustomUserDetails;
import com.bird.cos.service.admin.common.CommonCodeService;
import com.bird.cos.service.order.OrderItemService;
import com.bird.cos.service.post.PostService;
import com.bird.cos.service.scrap.ScrapService;
import lombok.Builder;
import lombok.Getter;
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
    private final ScrapService scrapService;

    @GetMapping
    public String getPostPage(PostSearchRequest searchRequest,
                              Model model,
                              @PageableDefault(size = 12, sort = "postCreatedAt", direction = Sort.Direction.DESC) Pageable pageable,
                              @AuthenticationPrincipal CustomUserDetails userDetails)
    {
        Long currentUserId = userDetails != null ? userDetails.getUserId() : null;
        Page<PostResponse> posts = postService.getPosts(searchRequest, pageable, currentUserId);

        model.addAttribute("posts", posts);
        model.addAttribute("searchRequest", searchRequest);
        model.addAttribute("currentUserId", currentUserId);
        
        // 필터 옵션 추가 (게시글 작성할 때 사용하는 것과 동일)
        model.addAttribute("housingTypes", getHousingTypeOptions());
        model.addAttribute("familyTypes", getFamilyTypeOptions());
        model.addAttribute("projectTypes", getProjectTypeOptions());
        model.addAttribute("roomCounts", getRoomCountOptions());
        model.addAttribute("familyCounts", getFamilyCountOptions());

        return "posts/index";
    }

    
    @Getter
    @Builder
    public static class FilterOption {
        private String value;
        private String displayName;
        
        public FilterOption(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/new")
    public String getNewPostPage(Model model)
    {
        List<CommonCode> postInformation = commonCodeService.getCommonCodeList("POST_INFORMATION");
        model.addAttribute("postInformation", postInformation);
        
        // 필터 옵션 추가
        model.addAttribute("housingTypes", getHousingTypeOptions());
        model.addAttribute("familyTypes", getFamilyTypeOptions());
        model.addAttribute("projectTypes", getProjectTypeOptions());

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

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{postId}/scrap")
    @ResponseBody
    public ResponseEntity<ScrapResponse> toggleScrap(@PathVariable Long postId,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean isScraped = scrapService.toggleScrap(userDetails.getUserId(), postId);
        long scrapCount = scrapService.getScrapCount(postId);
        
        return ResponseEntity.ok(ScrapResponse.builder()
                .isScraped(isScraped)
                .scrapCount(scrapCount)
                .build());
    }

    @Getter
    @Builder
    public static class ScrapResponse {
        private boolean isScraped;
        private long scrapCount;
    }


    // 주거형태 옵션
    private List<FilterOption> getHousingTypeOptions() {
        return List.of(
                new FilterOption("원룸", "원룸"),
                new FilterOption("오피스텔", "오피스텔"),
                new FilterOption("아파트", "아파트"),
                new FilterOption("빌라/연립", "빌라/연립"),
                new FilterOption("단독주택", "단독주택"),
                new FilterOption("기타", "기타")
        );
    }

    // 가족형태 옵션
    private List<FilterOption> getFamilyTypeOptions() {
        return List.of(
                new FilterOption("싱글", "싱글"),
                new FilterOption("신혼부부", "신혼부부"),
                new FilterOption("영유아 자녀", "영유아 자녀"),
                new FilterOption("초등학생 자녀", "초등학생 자녀"),
                new FilterOption("중고등학생 자녀", "중고등학생 자녀"),
                new FilterOption("부모님과 함께", "부모님과 함께")
        );
    }

    // 작업분야 옵션
    private List<FilterOption> getProjectTypeOptions() {
        return List.of(
                new FilterOption("전체 리모델링", "전체 리모델링"),
                new FilterOption("부분 리모델링", "부분 리모델링"),
                new FilterOption("홈스타일링", "홈스타일링"),
                new FilterOption("부분시공", "부분시공"),
                new FilterOption("DIY", "DIY")
        );
    }

    // 방 개수 옵션
    private List<FilterOption> getRoomCountOptions() {
        return List.of(
                new FilterOption("1", "1개"),
                new FilterOption("2", "2개"),
                new FilterOption("3", "3개"),
                new FilterOption("4", "4개 이상")
        );
    }

    // 가족 구성원 수 옵션
    private List<FilterOption> getFamilyCountOptions() {
        return List.of(
                new FilterOption("1", "1명"),
                new FilterOption("2", "2명"),
                new FilterOption("3", "3명"),
                new FilterOption("4", "4명"),
                new FilterOption("5", "5명 이상")
        );
    }
}
