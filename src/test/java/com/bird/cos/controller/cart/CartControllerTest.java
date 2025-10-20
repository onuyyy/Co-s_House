package com.bird.cos.controller.cart;

import com.bird.cos.domain.user.User;
import com.bird.cos.dto.cart.CartItemResponseDto;
import com.bird.cos.dto.cart.CartListResponse;
import com.bird.cos.dto.cart.CartSummaryDto;
import com.bird.cos.dto.cart.CheckoutInfoResponse;
import com.bird.cos.repository.user.UserRepository;
import com.bird.cos.service.cart.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * 결제 정보 조회 등 CartController의 핵심 엔드포인트를 검증한다.
 * currentUser 로직이 올바른 사용자를 찾고, Checkout 응답에 장바구니 요약이 반영되는지 확인한다.
 */
@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartController cartController;

    @Test
    void checkoutInfo_WithValidUser_ReturnsCheckoutSummary() {
        // 결제 정보 조회 시 사용자 정보와 장바구니 아이템이 응답에 정확히 매핑되는지 확인
        String email = "buyer@example.com";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        User user = User.builder()
                .userId(10L)
                .userEmail(email)
                .userName("구매자")
                .userPhone("010-0000-0000")
                .userAddress("서울시 강남구")
                .build();
        when(userRepository.findByUserEmail(email)).thenReturn(Optional.of(user));

        CartItemResponseDto item = CartItemResponseDto.builder()
                .cartItemId(1L)
                .productId(101L)
                .title("테스트 상품")
                .quantity(3)
                .build();
        CartListResponse cartResponse = new CartListResponse(
                List.of(item),
                CartSummaryDto.builder().totalQuantity(3).build()
        );
        when(cartService.getCart(user)).thenReturn(cartResponse);

        ResponseEntity<CheckoutInfoResponse> response = cartController.checkoutInfo(authentication);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        CheckoutInfoResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.userName()).isEqualTo("구매자");
        assertThat(body.userPhone()).isEqualTo("010-0000-0000");
        assertThat(body.userAddress()).isEqualTo("서울시 강남구");
        assertThat(body.items()).hasSize(1);
        assertThat(body.items().get(0).productId()).isEqualTo(101L);
        assertThat(body.items().get(0).title()).isEqualTo("테스트 상품");
        assertThat(body.items().get(0).quantity()).isEqualTo(3);

        verify(cartService).getCart(user);
    }

    @Test
    void checkoutInfo_WhenUserMissing_ThrowsRuntimeException() {
        // 인증된 이메일로 사용자를 찾을 수 없으면 예외를 던져야 한다
        String email = "missing@example.com";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByUserEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartController.checkoutInfo(authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(cartService, never()).getCart(any());
    }
}
