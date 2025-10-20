package com.bird.cos.service.product;

import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.product.ProductLike;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.mypage.MyLikedProductResponse;
import com.bird.cos.repository.product.ProductLikeRepository;
import com.bird.cos.repository.product.ProductRepository;
import com.bird.cos.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductLikeServiceTest {

    @Mock
    private ProductLikeRepository productLikeRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProductLikeService productLikeService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1L)
                .userEmail("user@example.com")
                .userNickname("tester")
                .userName("테스터")
                .build();

        product = Product.builder()
                .productId(100L)
                .productTitle("테스트 상품")
                .mainImageUrl("https://image")
                .originalPrice(BigDecimal.valueOf(10000))
                .salePrice(BigDecimal.valueOf(8000))
                .bookmarkCount(0)
                .build();
    }

    // 좋아요가 없는 상태에서 토글 시 저장과 북마크 수 증가가 일어나는지 확인
    @Test
    void toggleLike_WhenNotLikedYet_SavesLikeAndIncrementsBookmark() {
        when(productLikeRepository.findByUser_UserIdAndProduct_ProductId(1L, 100L))
                .thenReturn(Optional.empty());
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean liked = productLikeService.toggleLike(1L, 100L);

        assertThat(liked).isTrue();
        assertThat(product.getBookmarkCount()).isEqualTo(1);

        ArgumentCaptor<ProductLike> captor = ArgumentCaptor.forClass(ProductLike.class);
        verify(productLikeRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getProduct()).isEqualTo(product);
        verify(productRepository).save(product);
    }

    // 이미 좋아요가 있는 상태에서 토글 시 삭제와 북마크 수 감소가 일어나는지 확인
    @Test
    void toggleLike_WhenAlreadyLiked_DeletesLikeAndDecrementsBookmark() {
        product.increaseBookmarkCount();
        product.increaseBookmarkCount();
        ProductLike existing = ProductLike.builder()
                .id(10L)
                .product(product)
                .user(user)
                .build();
        when(productLikeRepository.findByUser_UserIdAndProduct_ProductId(1L, 100L))
                .thenReturn(Optional.of(existing));

        boolean liked = productLikeService.toggleLike(1L, 100L);

        assertThat(liked).isFalse();
        assertThat(product.getBookmarkCount()).isEqualTo(1);
        verify(productLikeRepository).delete(existing);
        verify(productRepository).save(product);
    }

    // 존재하지 않는 상품에 좋아요를 시도하면 예외가 발생하는지 확인
    @Test
    void toggleLike_WhenProductNotFound_ThrowsException() {
        when(productLikeRepository.findByUser_UserIdAndProduct_ProductId(1L, 200L))
                .thenReturn(Optional.empty());
        when(productRepository.findById(200L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productLikeService.toggleLike(1L, 200L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품을 찾을 수 없습니다.");
    }

    // 특정 상품의 좋아요 수가 리포지터리 값과 동일하게 반환되는지 확인
    @Test
    void countLikes_ReturnsRepositoryValue() {
        when(productLikeRepository.countByProduct_ProductId(100L)).thenReturn(5L);

        long count = productLikeService.countLikes(100L);

        assertThat(count).isEqualTo(5L);
    }

    // 사용자 ID가 없을 때는 좋아요 여부가 false로 반환되는지 확인
    @Test
    void isLiked_WhenUserIdNull_ReturnsFalse() {
        boolean result = productLikeService.isLiked(null, 100L);

        assertThat(result).isFalse();
        verify(productLikeRepository, never()).existsByUser_UserIdAndProduct_ProductId(any(), any());
    }

    // 사용자 ID와 상품 ID가 주어졌을 때 좋아요 여부를 리포지터리 값대로 반환하는지 확인
    @Test
    void isLiked_WhenUserExists_DelegatesToRepository() {
        when(productLikeRepository.existsByUser_UserIdAndProduct_ProductId(1L, 100L)).thenReturn(true);

        boolean result = productLikeService.isLiked(1L, 100L);

        assertThat(result).isTrue();
    }

    // 좋아요 목록 조회 시 엔티티가 DTO로 정상 매핑되는지 확인
    @Test
    void getLikedProducts_ReturnsMappedPage() {
        ProductLike like = ProductLike.builder()
                .id(11L)
                .user(user)
                .product(product)
                .createdAt(LocalDateTime.now())
                .build();
        Pageable pageable = PageRequest.of(0, 10);
        when(productLikeRepository.findByUser_UserId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(like), pageable, 1));

        Page<MyLikedProductResponse> page = productLikeService.getLikedProducts(1L, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        MyLikedProductResponse response = page.getContent().get(0);
        assertThat(response.getLikeId()).isEqualTo(11L);
        assertThat(response.getProductId()).isEqualTo(100L);
        assertThat(response.getProductTitle()).isEqualTo("테스트 상품");
    }

    // 사용자 ID가 없을 때 좋아요 개수는 0으로 처리되는지 확인
    @Test
    void countLikedProducts_WhenUserIdNull_ReturnsZero() {
        long count = productLikeService.countLikedProducts(null);

        assertThat(count).isZero();
        verify(productLikeRepository, never()).countByUser_UserId(any());
    }

    // 사용자 ID가 있을 때 좋아요 개수가 리포지터리 값과 동일한지 확인
    @Test
    void countLikedProducts_WhenUserIdExists_ReturnsRepositoryValue() {
        when(productLikeRepository.countByUser_UserId(1L)).thenReturn(3L);

        long count = productLikeService.countLikedProducts(1L);

        assertThat(count).isEqualTo(3L);
    }

    // 사용자 ID나 상품 ID 목록이 없을 때 빈 목록을 반환하는지 확인
    @Test
    void getLikedProductIds_WhenInputInvalid_ReturnsEmptyList() {
        assertThat(productLikeService.getLikedProductIds(null, List.of(1L, 2L))).isEmpty();
        assertThat(productLikeService.getLikedProductIds(1L, null)).isEmpty();
        assertThat(productLikeService.getLikedProductIds(1L, List.of())).isEmpty();
        verify(productLikeRepository, never()).findByUser_UserIdAndProduct_ProductIdIn(any(), any());
    }

    // 사용자 ID와 상품 목록이 주어졌을 때 좋아요된 상품 ID만 반환하는지 확인
    @Test
    void getLikedProductIds_WhenInputValid_ReturnsMatchedIds() {
        ProductLike like1 = ProductLike.builder().product(product).user(user).build();
        Product otherProduct = Product.builder().productId(200L).build();
        ProductLike like2 = ProductLike.builder().product(otherProduct).user(user).build();

        when(productLikeRepository.findByUser_UserIdAndProduct_ProductIdIn(1L, List.of(100L, 200L)))
                .thenReturn(List.of(like1, like2));

        List<Long> result = productLikeService.getLikedProductIds(1L, List.of(100L, 200L));

        assertThat(result).containsExactlyInAnyOrder(100L, 200L);
    }
}

