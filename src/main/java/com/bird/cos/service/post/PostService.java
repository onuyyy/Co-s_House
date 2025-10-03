package com.bird.cos.service.post;

import com.bird.cos.domain.post.Post;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.post.PostRequest;
import com.bird.cos.repository.post.PostImageRepository;
import com.bird.cos.repository.post.PostRepository;
import com.bird.cos.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class PostService {

    private final PostImageRepository postImageRepository;
    private final PostRepository postRepository;
    private final AuthService authService;

    public void createPost(PostRequest postRequest, long userId) {
        User user = authService.getUser(userId);
        Post save = postRepository.save(Post.from(postRequest, user));

        if (postRequest.getImages() != null && !postRequest.getImages().isEmpty()) {
            uploadImage(postRequest.getImages(), save.getPostId());
        }
    }

    public String uploadImage(List<MultipartFile> images, long postId) {
        for (MultipartFile file : images) {
        }
        return null;
    }
}
