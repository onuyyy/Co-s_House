package com.bird.cos.service.cart;

import com.bird.cos.domain.cart.Cart;
import com.bird.cos.domain.cart.CartItem;
import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.product.ProductOption;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.cart.AddToCartRequest;
import com.bird.cos.repository.cart.CartHeaderRepository;
import com.bird.cos.repository.cart.CartItemRepository;
import com.bird.cos.repository.mypage.coupon.UserCouponRepository;
import com.bird.cos.repository.product.ProductOptionRepository;
import com.bird.cos.repository.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartHeaderRepository cartHeaderRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private UserCouponRepository userCouponRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1L)
                .userEmail("user@example.com")
                .userNickname("tester")
                .userName("테스터")
                .build();
        cart = Cart.of(user);
    }

    @Test
    void addCart_WithNewOption_CreatesCartItemWithSelection() {
        Long productId = 100L;
        Long optionId = 200L;

        Product product = mock(Product.class);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getProductId()).thenReturn(productId);
        when(product.getStockQuantity()).thenReturn(10);

        ProductOption option = ProductOption.builder()
                .optionId(optionId)
                .product(product)
                .optionName("색상")
                .optionValue("블랙")
                .build();
        when(productOptionRepository.findById(optionId)).thenReturn(Optional.of(option));

        when(cartHeaderRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartAndProductAndSelectedOptions(cart, product, optionId.toString()))
                .thenReturn(Optional.empty());

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        when(cartItemRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        AddToCartRequest request = new AddToCartRequest(productId, 2, optionId.toString());

        cartService.addCart(request, user);

        CartItem saved = captor.getValue();
        assertThat(saved.getSelectedOptions()).isEqualTo(optionId.toString());
        assertThat(saved.getQuantity()).isEqualTo(2);
    }

    @Test
    void addCart_WithExistingOption_IncreasesQuantity() {
        Long productId = 500L;
        Long optionId = 600L;

        Product product = mock(Product.class);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getProductId()).thenReturn(productId);
        when(product.getStockQuantity()).thenReturn(50);

        ProductOption option = ProductOption.builder()
                .optionId(optionId)
                .product(product)
                .optionName("사이즈")
                .optionValue("M")
                .build();
        when(productOptionRepository.findById(optionId)).thenReturn(Optional.of(option));

        when(cartHeaderRepository.findByUser(user)).thenReturn(Optional.of(cart));

        CartItem existing = CartItem.of(cart, product, 1, optionId.toString());
        when(cartItemRepository.findByCartAndProductAndSelectedOptions(cart, product, optionId.toString()))
                .thenReturn(Optional.of(existing));

        when(cartItemRepository.save(existing)).thenReturn(existing);

        AddToCartRequest request = new AddToCartRequest(productId, 3, optionId.toString());

        cartService.addCart(request, user);

        assertThat(existing.getQuantity()).isEqualTo(4);
        assertThat(existing.getSelectedOptions()).isEqualTo(optionId.toString());
        verify(cartItemRepository).save(existing);
    }

    @Test
    void addCart_WithMismatchedOption_ThrowsException() {
        Long productId = 900L;
        Long optionId = 901L;

        Product product = mock(Product.class);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getProductId()).thenReturn(productId);

        Product otherProduct = mock(Product.class);
        when(otherProduct.getProductId()).thenReturn(777L);

        ProductOption option = ProductOption.builder()
                .optionId(optionId)
                .product(otherProduct)
                .optionName("색상")
                .optionValue("레드")
                .build();
        when(productOptionRepository.findById(optionId)).thenReturn(Optional.of(option));

        AddToCartRequest request = new AddToCartRequest(productId, 1, optionId.toString());

        assertThatThrownBy(() -> cartService.addCart(request, user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("해당 상품의 옵션이 아닙니다.");

        verify(cartItemRepository, never()).save(any());
    }
}
