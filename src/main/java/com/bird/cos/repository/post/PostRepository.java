package com.bird.cos.repository.post;

import com.bird.cos.domain.post.Post;
import com.bird.cos.service.home.dto.HomePostDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    @Query("select new com.bird.cos.service.home.dto.HomePostDto(p.postId, p.title, p.likeCount, p.commentCount) " +
            "from Post p where p.isPublic = true order by p.likeCount desc, p.viewCount desc")
    List<HomePostDto> findTopPublic(Pageable pageable);
}
