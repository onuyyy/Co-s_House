package com.bird.cos.controller.product;

import com.bird.cos.security.CustomUserDetails;
import com.bird.cos.service.product.ProductLikeService;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductLikeController {

    private final ProductLikeService productLikeService;

    @PostMapping("/{productId}/like")
    public ResponseEntity<LikeResponse> toggleLike(@PathVariable Long productId,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean liked = productLikeService.toggleLike(userDetails.getUserId(), productId);
        long likeCount = productLikeService.countLikes(productId);
        return ResponseEntity.ok(newLikeResponse(liked, likeCount));
    }

    @GetMapping("/{productId}/like")
    public ResponseEntity<LikeResponse> getLikeStatus(@PathVariable Long productId,
                                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;
        boolean liked = productLikeService.isLiked(userId, productId);
        long likeCount = productLikeService.countLikes(productId);
        return ResponseEntity.ok(newLikeResponse(liked, likeCount));
    }

    private LikeResponse newLikeResponse(boolean liked, long count) {
        return LikeResponse.builder()
                .liked(liked)
                .likeCount(count)
                .build();
    }

    @Getter
    @Builder
    public static class LikeResponse {
        private final boolean liked;
        private final long likeCount;
    }
}

