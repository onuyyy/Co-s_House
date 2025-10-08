package com.bird.cos.controller.post;

import com.bird.cos.dto.post.CommentResponse;
import com.bird.cos.dto.post.WriteCommentRequest;
import com.bird.cos.security.CustomUserDetails;
import com.bird.cos.service.post.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @ResponseBody
    public ResponseEntity<Void> writeComment(@RequestBody WriteCommentRequest request,
                                             @AuthenticationPrincipal CustomUserDetails customUserDetails)
    {
        commentService.writeComment(request, customUserDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{postId}")
    @ResponseBody
    public ResponseEntity<List<CommentResponse>> getPostComments(@PathVariable Long postId) {
        List<CommentResponse> postComments = commentService.getPostComments(postId);
        return ResponseEntity.ok(postComments);
    }

}
