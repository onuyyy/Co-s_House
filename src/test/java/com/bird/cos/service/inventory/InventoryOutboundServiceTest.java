package com.bird.cos.service.inventory;

import com.bird.cos.domain.inventory.Inventory;
import com.bird.cos.domain.inventory.InventoryOutbound;
import com.bird.cos.domain.order.Order;
import com.bird.cos.domain.order.OrderItem;
import com.bird.cos.domain.product.Product;
import com.bird.cos.exception.BusinessException;
import com.bird.cos.repository.inventory.InventoryHistoryRepository;
import com.bird.cos.repository.inventory.InventoryOutboundRepository;
import com.bird.cos.repository.inventory.InventoryRepository;
import com.bird.cos.repository.order.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InventoryOutboundServiceTest {

    @Mock
    private InventoryOutboundRepository inventoryOutboundRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryHistoryRepository inventoryHistoryRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private InventoryOutboundService inventoryOutboundService;

    private Product product;
    private Inventory inventory;
    private Order order;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .productId(100L)
                .productTitle("테스트 상품")
                .stockQuantity(50)
                .build();

        // Inventory Mock 설정
        inventory = mock(Inventory.class);
        when(inventory.getCurrentQuantity()).thenReturn(100);

        orderItem = OrderItem.builder()
                .product(product)
                .quantity(5)
                .build();

        order = Order.builder()
                .orderId(1L)
                .orderItems(List.of(orderItem))
                .build();
    }

    @Test
    void processOutboundForOrder_출고처리_성공() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(inventoryRepository.findFirstByProductIdOrderByInventoryIdAsc(product)).thenReturn(Optional.of(inventory));
        when(inventoryOutboundRepository.save(any(InventoryOutbound.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        inventoryOutboundService.processOutboundForOrder(1L);

        // Then
        verify(inventoryOutboundRepository).save(any(InventoryOutbound.class));
        verify(inventoryRepository).save(inventory);
        verify(inventoryHistoryRepository).save(any());
    }

    @Test
    void processOutboundForOrder_존재하지않는주문_예외발생() {
        // Given
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryOutboundService.processOutboundForOrder(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("존재하지 않는 주문입니다");

        verify(inventoryOutboundRepository, never()).save(any());
    }

    @Test
    void processInboundForOrder_취소반품_재고복구_성공() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(inventoryRepository.findFirstByProductIdOrderByInventoryIdAsc(product)).thenReturn(Optional.of(inventory));
        when(inventoryOutboundRepository.save(any(InventoryOutbound.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        inventoryOutboundService.processInboundForOrder(1L);

        // Then
        verify(inventoryOutboundRepository).save(any(InventoryOutbound.class));
        verify(inventoryRepository).save(inventory);
        verify(inventoryHistoryRepository).save(any());
    }

    @Test
    void processInboundForOrder_존재하지않는주문_예외발생() {
        // Given
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryOutboundService.processInboundForOrder(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("존재하지 않는 주문입니다");

        verify(inventoryOutboundRepository, never()).save(any());
    }
}
