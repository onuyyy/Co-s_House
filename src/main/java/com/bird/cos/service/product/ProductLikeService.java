package com.bird.cos.service.product;

import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.product.ProductLike;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.mypage.MyLikedProductResponse;
import com.bird.cos.repository.product.ProductLikeRepository;
import com.bird.cos.repository.product.ProductRepository;
import com.bird.cos.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductLikeService {

    private final ProductLikeRepository productLikeRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean toggleLike(Long userId, Long productId) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(productId, "productId must not be null");

        return productLikeRepository.findByUser_UserIdAndProduct_ProductId(userId, productId)
                .map(existing -> {
                    productLikeRepository.delete(existing);
                    decrementBookmarkSafely(existing.getProduct());
                    return false;
                })
                .orElseGet(() -> {
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

                    productLikeRepository.save(ProductLike.of(user, product));
                    incrementBookmarkSafely(product);
                    return true;
                });
    }

    @Transactional(readOnly = true)
    public long countLikes(Long productId) {
        return productLikeRepository.countByProduct_ProductId(productId);
    }

    @Transactional(readOnly = true)
    public boolean isLiked(Long userId, Long productId) {
        if (userId == null) {
            return false;
        }
        return productLikeRepository.existsByUser_UserIdAndProduct_ProductId(userId, productId);
    }

    @Transactional(readOnly = true)
    public Page<MyLikedProductResponse> getLikedProducts(Long userId, Pageable pageable) {
        return productLikeRepository.findByUser_UserId(userId, pageable)
                .map(MyLikedProductResponse::from);
    }

    @Transactional(readOnly = true)
    public long countLikedProducts(Long userId) {
        if (userId == null) {
            return 0;
        }
        return productLikeRepository.countByUser_UserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Long> getLikedProductIds(Long userId, List<Long> productIds) {
        if (userId == null || productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return productLikeRepository.findByUser_UserIdAndProduct_ProductIdIn(userId, productIds).stream()
                .map(like -> like.getProduct().getProductId())
                .toList();
    }

    private void incrementBookmarkSafely(Product product) {
        if (product == null) {
            return;
        }
        product.increaseBookmarkCount();
        productRepository.save(product);
    }

    private void decrementBookmarkSafely(Product product) {
        if (product == null) {
            return;
        }
        product.decreaseBookmarkCount();
        productRepository.save(product);
    }
}
