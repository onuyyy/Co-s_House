package com.bird.cos.repository.post;

import com.bird.cos.domain.post.Post;
import com.bird.cos.dto.post.PostSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {
    Page<Post> searchPosts(PostSearchRequest request, Pageable pageable);
}
