package com.bird.cos.repository.post;

import com.bird.cos.domain.post.PostProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostProductRepository extends JpaRepository<PostProduct, Long> {
    
    @Query("SELECT pp FROM PostProduct pp JOIN FETCH pp.product WHERE pp.post.postId = :postId ORDER BY pp.displayOrder")
    List<PostProduct> findByPost_PostId(@Param("postId") Long postId);
}
