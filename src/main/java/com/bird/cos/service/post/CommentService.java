package com.bird.cos.service.post;

import com.bird.cos.domain.post.Comment;
import com.bird.cos.domain.post.Post;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.post.CommentResponse;
import com.bird.cos.dto.post.WriteCommentRequest;
import com.bird.cos.exception.BusinessException;
import com.bird.cos.repository.post.CommentRepository;
import com.bird.cos.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final AuthService authService;
    private final PostService postService;

    @Transactional
    public void writeComment(WriteCommentRequest request, Long userId) {
        Post post = postService.findPost(request.getPostId());
        User user = authService.getUser(userId);

        Comment.CommentBuilder commentBuilder = Comment.builder()
                .post(post)
                .user(user)
                .content(request.getContent());

        // parentCommentId가 있으면 대댓글, 없으면 신규 댓글
        if (request.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> BusinessException.notFoundComment(request.getParentCommentId()));
            commentBuilder.parentCommentId(parentComment);
        }

        Comment comment = commentBuilder.build();
        commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getPostComments(Long postId) {
        // 해당 게시글의 모든 댓글 조회
        List<Comment> allComments = commentRepository.findByPost_PostId(postId);

        // 최상위 댓글만 필터링 (parentCommentId가 null인 댓글)
        List<Comment> rootComments = allComments.stream()
                .filter(comment -> comment.getParentCommentId() == null)
                .toList();

        // 각 최상위 댓글에 대해 대댓글을 포함한 CommentResponse 생성
        return rootComments.stream()
                .map(comment -> buildCommentResponse(comment, allComments))
                .collect(Collectors.toList());
    }

    private CommentResponse buildCommentResponse(Comment comment, List<Comment> allComments) {
        // 현재 댓글의 자식 댓글 찾기
        List<CommentResponse> childResponses = allComments.stream()
                .filter(c -> c.getParentCommentId() != null &&
                            c.getParentCommentId().getCommentId().equals(comment.getCommentId()))
                .map(childComment -> buildCommentResponse(childComment, allComments))
                .collect(Collectors.toList());

        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .content(comment.getContent())
                .username(comment.getUser().getNickname())
                .userId(comment.getUser().getUserId())
                .createdAt(comment.getCommentCreatedAt())
                .childComments(childResponses)
                .build();
    }
}
