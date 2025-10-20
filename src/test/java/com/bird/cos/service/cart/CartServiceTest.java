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
import com.bird.cos.repository.inventory.InventoryRepository;
import com.bird.cos.repository.product.ProductOptionRepository;
import com.bird.cos.repository.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * CartService 주요 시나리오(추가/수정/삭제/병합 등)를 검증하는 단위 테스트.
 * 서비스 레이어의 재고 검증, 옵션 일치 여부, 수량 제한, 게스트 카트 병합 흐름을 집중적으로 다룬다.
 */
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

    @Mock
    private InventoryRepository inventoryRepository;

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
        // 신규 옵션으로 담을 때 옵션 선택과 수량 누적이 제대로 저장되는지 확인
        Long productId = 100L;
        Long optionId = 200L;

        Product product = mock(Product.class);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getProductId()).thenReturn(productId);
        when(product.getStockQuantity()).thenReturn(10);
        when(inventoryRepository.findByProductId(product)).thenReturn(Optional.empty());

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
        // 동일 옵션을 다시 담으면 수량이 누적 저장되는지 확인
        Long productId = 500L;
        Long optionId = 600L;

        Product product = mock(Product.class);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getProductId()).thenReturn(productId);
        when(product.getStockQuantity()).thenReturn(50);
        when(inventoryRepository.findByProductId(product)).thenReturn(Optional.empty());

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
        // 다른 상품의 옵션을 선택했을 때 즉시 예외가 발생하는지 검증
        Long productId = 900L;
        Long optionId = 901L;

        Product product = mock(Product.class);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getProductId()).thenReturn(productId);
        when(inventoryRepository.findByProductId(product)).thenReturn(Optional.empty());

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

    @Test
    void addCart_WhenDesiredQuantityExceedsStock_ThrowsException() {
        // 재고보다 많은 수량을 담으려 하면 예외가 발생해야 한다
        Long productId = 321L;

        Product product = mock(Product.class);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getProductId()).thenReturn(productId);
        when(product.getStockQuantity()).thenReturn(3);
        when(inventoryRepository.findByProductId(product)).thenReturn(Optional.empty());

        when(cartHeaderRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartAndProductAndSelectedOptions(eq(cart), eq(product), any()))
                .thenReturn(Optional.empty());
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());

        AddToCartRequest request = new AddToCartRequest(productId, 5, null);

        assertThatThrownBy(() -> cartService.addCart(request, user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("재고 부족: 최대 3개까지 담을 수 있습니다.");
    }

    @Test
    void updateQuantity_WithValidInput_PersistsNormalizedQuantity() {
        // 수량 변경 요청이 재고 범위 안이면 정상 저장되어야 한다
        Long cartItemId = 11L;
        Product product = Product.builder()
                .productId(501L)
                .stockQuantity(20)
                .build();
        CartItem item = CartItem.of(cart, product, 2, null);

        when(cartItemRepository.findByCartItemIdAndCart_User(cartItemId, user)).thenReturn(Optional.of(item));
        when(inventoryRepository.findByProductId(product)).thenReturn(Optional.empty());

        cartService.updateQuantity(cartItemId, user, 5);

        assertThat(item.getQuantity()).isEqualTo(5);
        verify(cartItemRepository).save(item);
    }

    @Test
    void updateQuantity_WithExcessiveValue_ThrowsException() {
        // 최대 허용 수량을 초과하면 예외가 발생해야 한다
        Long cartItemId = 12L;
        Product product = Product.builder()
                .productId(601L)
                .stockQuantity(5)
                .build();
        CartItem item = CartItem.of(cart, product, 2, null);

        when(cartItemRepository.findByCartItemIdAndCart_User(cartItemId, user)).thenReturn(Optional.of(item));
        when(inventoryRepository.findByProductId(product)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateQuantity(cartItemId, user, 10))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("재고 부족: 최대 5개까지 담을 수 있습니다.");

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void delete_WhenItemsExist_DeletesAll() {
        // 삭제 대상으로 조회된 항목이 있으면 모두 제거해야 한다
        Long cartItemId = 91L;
        Product product = Product.builder().productId(901L).build();
        CartItem item = CartItem.of(cart, product, 1, null);

        when(cartItemRepository.findAllByCartItemIdInAndCart_User(List.of(cartItemId), user))
                .thenReturn(List.of(item));

        cartService.delete(List.of(cartItemId), user);

        verify(cartItemRepository).deleteAll(List.of(item));
    }

    @Test
    void delete_WhenItemsMissing_ThrowsException() {
        // 삭제 대상이 없으면 사용자에게 오류가 전달되어야 한다
        Long cartItemId = 92L;
        when(cartItemRepository.findAllByCartItemIdInAndCart_User(List.of(cartItemId), user))
                .thenReturn(List.of());

        assertThatThrownBy(() -> cartService.delete(List.of(cartItemId), user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("삭제할 항목이 존재하지 않습니다.");
    }

    @Test
    void updateOptions_WithValidOption_UpdatesSelection() {
        // 옵션 변경 요청이 동일 상품의 옵션이면 선택값이 갱신되어야 한다
        Long cartItemId = 41L;
        Long optionId = 55L;
        Product product = Product.builder().productId(777L).stockQuantity(5).build();
        CartItem item = CartItem.of(cart, product, 2, null);

        ProductOption option = ProductOption.builder()
                .optionId(optionId)
                .product(product)
                .optionName("색상")
                .optionValue("블루")
                .build();

        when(cartItemRepository.findByCartItemIdAndCart_User(cartItemId, user)).thenReturn(Optional.of(item));
        when(productOptionRepository.findById(optionId)).thenReturn(Optional.of(option));
        when(inventoryRepository.findByProductId(product)).thenReturn(Optional.empty());

        cartService.updateOptions(cartItemId, user, optionId.toString());

        assertThat(item.getSelectedOptions()).isEqualTo(optionId.toString());
        verify(cartItemRepository).save(item);
    }

    @Test
    void updateOptions_WithMismatchedProduct_ThrowsException() {
        // 다른 상품의 옵션을 적용하려 하면 예외가 발생해야 한다
        Long cartItemId = 42L;
        Long optionId = 66L;
        Product product = Product.builder().productId(888L).stockQuantity(5).build();
        Product otherProduct = Product.builder().productId(999L).build();
        CartItem item = CartItem.of(cart, product, 2, null);

        ProductOption option = ProductOption.builder()
                .optionId(optionId)
                .product(otherProduct)
                .optionName("사이즈")
                .optionValue("L")
                .build();

        when(cartItemRepository.findByCartItemIdAndCart_User(cartItemId, user)).thenReturn(Optional.of(item));
        when(productOptionRepository.findById(optionId)).thenReturn(Optional.of(option));

        assertThatThrownBy(() -> cartService.updateOptions(cartItemId, user, optionId.toString()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("해당 상품의 옵션이 아닙니다.");

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void mergeGuestCart_WithMultipleItems_AppliesQuantitiesUntilStockExceeded() {
        // 게스트 장바구니 합치기 시 수량이 누적되다가 재고 제한을 넘어가면 예외가 발생해야 한다
        Long firstProductId = 301L;
        Long secondProductId = 302L;

        Product firstProduct = Product.builder().productId(firstProductId).stockQuantity(10).build();
        Product secondProduct = Product.builder().productId(secondProductId).stockQuantity(2).build();

        CartItem existingSecond = CartItem.of(cart, secondProduct, 1, null);

        when(cartHeaderRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(firstProductId)).thenReturn(Optional.of(firstProduct));
        when(productRepository.findById(secondProductId)).thenReturn(Optional.of(secondProduct));

        when(inventoryRepository.findByProductId(firstProduct)).thenReturn(Optional.empty());
        when(inventoryRepository.findByProductId(secondProduct)).thenReturn(Optional.empty());

        when(cartItemRepository.findByCartAndProductAndSelectedOptions(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(cartItemRepository.findByCartAndProduct(cart, firstProduct)).thenReturn(Optional.empty());
        when(cartItemRepository.findByCartAndProduct(cart, secondProduct)).thenReturn(Optional.of(existingSecond));

        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<CartService.GuestItem> guestItems = List.of(
                new CartService.GuestItem(firstProductId, 4, null),
                new CartService.GuestItem(secondProductId, 5, null)
        );

        assertThatThrownBy(() -> cartService.mergeGuestCart(guestItems, user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("재고 부족: 최대 2개까지 담을 수 있습니다.");

        verify(cartItemRepository, atLeastOnce()).save(any(CartItem.class));
    }
}
