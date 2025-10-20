package com.bird.cos.service.order;

import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.domain.order.Order;
import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.product.ProductOption;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.order.*;
import com.bird.cos.repository.common.CommonCodeRepository;
import com.bird.cos.repository.order.OrderRepository;
import com.bird.cos.repository.product.ProductOptionRepository;
import com.bird.cos.repository.product.ProductRepository;
import com.bird.cos.repository.user.UserRepository;
import com.bird.cos.service.cart.CartService;
import com.bird.cos.service.inventory.InventoryOutboundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CartService cartService;
    @Mock private CommonCodeRepository commonCodeRepository;
    @Mock private ProductOptionRepository productOptionRepository;
    @Mock private InventoryOutboundService inventoryOutboundService;

    @InjectMocks
    private OrderService orderService;

    private String email;
    private Long productId;
    private Long productOptionId;
    private Long cartItemId;

    private User mockUser;
    private Product mockProduct;
    private ProductOption mockProductOption;
    private OrderRequest orderRequest;
    private CommonCode mockStatusCode;
    private CommonCode mockDeliveryStatusCode;

    @BeforeEach
    void setUp() {
        email = "test@gmail.com";
        productId = 1L;
        productOptionId = 10L;
        cartItemId = 100L;

        mockUser = User.builder()
                .userId(1L)
                .userEmail(email)
                .userName("테스트유저")
                .userPhone("010-1234-5678")
                .userAddress("서울시 강남구")
                .build();

        mockProduct = Product.builder()
                .productId(productId)
                .productTitle("테스트 상품")
                .mainImageUrl("http://test-image.jpg")
                .originalPrice(BigDecimal.valueOf(10000))
                .build();

        mockProductOption = ProductOption.builder()
                .optionId(productOptionId)
                .product(mockProduct)
                .optionName("사이즈")
                .optionValue("M")
                .additionalPrice(BigDecimal.ZERO)
                .build();

        orderRequest = OrderRequest.builder()
                .productId(productId)
                .productOptionId(productOptionId)
                .quantity(2)
                .price(BigDecimal.valueOf(10000))
                .cartItemId(cartItemId)
                .build();

        mockStatusCode = CommonCode.builder()
                .codeId(OrderStatusCode.PAID.getCode())
                .codeName("PAID")
                .build();

        mockDeliveryStatusCode = CommonCode.builder()
                .codeId(DeliveryStatusCode.PREPARING.getCode())
                .codeName("배송 준비")
                .build();

    }

    @Test
    void givenEmailOrderRequest_WhenGetOrderPreview_ThenOrderPreviewResponse() {
        // given

        List<OrderRequest> orderItems = new ArrayList<>();
        orderItems.add(orderRequest);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(mockUser));
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(mockProduct));
        when(productOptionRepository.findById(productOptionId))
                .thenReturn(Optional.of(mockProductOption));

        // ========== WHEN (테스트 메서드 실행) ==========
        OrderPreviewResponse response = orderService.getOrderPreview(email, orderItems);

        // ========== THEN (결과 검증) ==========
        // 1. response가 null이 아닌지 검증
        assertThat(response).isNotNull();

        // 2. 유저 정보가 올바르게 설정되었는지 검증
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getUserName()).isEqualTo("테스트유저");
        assertThat(response.getUser().getUserEmail()).isEqualTo(email);
        assertThat(response.getUser().getPhone()).isEqualTo("010-1234-5678");

        // 3. 주문 아이템 정보가 올바르게 설정되었는지 검증
        assertThat(response.getItems()).isNotNull();
        assertThat(response.getItems()).hasSize(1);  // 아이템 개수 확인

        OrderPreviewResponse.OrderItemPreviewResponse firstItem = response.getItems().get(0);
        assertThat(firstItem.getProductId()).isEqualTo(productId);
        assertThat(firstItem.getProductName()).isEqualTo("테스트 상품");
        assertThat(firstItem.getProductOptionId()).isEqualTo(productOptionId);
        assertThat(firstItem.getProductOptionName()).isEqualTo("사이즈 : M");
        assertThat(firstItem.getQuantity()).isEqualTo(2);
        assertThat(firstItem.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(firstItem.getImageUrl()).isEqualTo("http://test-image.jpg");
        assertThat(firstItem.getCartItemId()).isEqualTo(cartItemId);

        // 4. 총 가격이 올바르게 계산되었는지 검증
        // 10000원 * 2개 = 20000원
        assertThat(response.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(20000));
    }

    @Test
    void givenOrderRequest_whenCreateOrder_thenOrderResponse() {

        // given
        Order mockOrder = Order.builder()
                .orderId(1L)
                .user(mockUser)
                .orderStatusCode(mockStatusCode)
                .totalAmount(BigDecimal.valueOf(20000))
                .paidAmount(BigDecimal.valueOf(20000))
                .orderDate(LocalDateTime.now())
                .build();

        List<OrderRequest> orderItems = List.of(orderRequest);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(mockUser));
        when(commonCodeRepository.findById(OrderStatusCode.PAID.getCode()))
                .thenReturn(Optional.of(mockStatusCode));
        when(commonCodeRepository.findById(DeliveryStatusCode.PREPARING.getCode()))
                .thenReturn(Optional.of(mockDeliveryStatusCode));
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(mockProduct));
        when(productOptionRepository.findById(productOptionId))
                .thenReturn(Optional.of(mockProductOption));
        when(orderRepository.save(any(Order.class)))
                .thenReturn(mockOrder);

        // 외부 서비스 호출은 동작만 검증 (예외 발생 X)
        doNothing().when(inventoryOutboundService).processOutboundForOrder(anyLong());
        doNothing().when(cartService).delete(anyList(), eq(mockUser));

        // when
        OrderResponse response = orderService.createOrder(
                email, orderItems, null, null, BigDecimal.ZERO, BigDecimal.valueOf(20000), List.of(cartItemId)
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getUser().getUserEmail()).isEqualTo(email);
        assertThat(response.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(20000));

        verify(orderRepository, times(2)).save(any(Order.class)); // 저장 2번 호출 (주문 생성 + 아이템 추가 후)
        verify(inventoryOutboundService, times(1)).processOutboundForOrder(1L); // 출고 처리
        verify(cartService, times(1)).delete(anyList(), eq(mockUser)); // 장바구니 정리

    }

}