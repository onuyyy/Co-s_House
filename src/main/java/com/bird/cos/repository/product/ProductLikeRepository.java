package com.bird.cos.repository.product;

import com.bird.cos.domain.product.ProductLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {

    Optional<ProductLike> findByUser_UserIdAndProduct_ProductId(Long userId, Long productId);

    long countByProduct_ProductId(Long productId);

    boolean existsByUser_UserIdAndProduct_ProductId(Long userId, Long productId);

    Page<ProductLike> findByUser_UserId(Long userId, Pageable pageable);

    List<ProductLike> findByUser_UserIdAndProduct_ProductIdIn(Long userId, List<Long> productIds);

    long countByUser_UserId(Long userId);
}
