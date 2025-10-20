package com.bird.cos.service.inventory;

import com.bird.cos.domain.inventory.Inventory;
import com.bird.cos.domain.inventory.InventoryReceipt;
import com.bird.cos.domain.product.Product;
import com.bird.cos.dto.admin.InventoryReceiptRequest;
import com.bird.cos.dto.admin.InventoryReceiptResponse;
import com.bird.cos.exception.BusinessException;
import com.bird.cos.repository.inventory.InventoryHistoryRepository;
import com.bird.cos.repository.inventory.InventoryReceiptRepository;
import com.bird.cos.repository.inventory.InventoryRepository;
import com.bird.cos.repository.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InventoryReceiptServiceTest {

    @Mock
    private InventoryReceiptRepository inventoryReceiptRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryHistoryRepository inventoryHistoryRepository;

    @InjectMocks
    private InventoryReceiptService inventoryReceiptService;

    private Product product;
    private Inventory inventory;

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
    }

    @Test
    void processReceipt_입고처리_COMPLETED상태_재고증가() {
        // Given
        InventoryReceiptRequest request = InventoryReceiptRequest.of(
                100L,
                10,
                LocalDate.now(),
                "COMPLETED"
        );

        InventoryReceipt receipt = InventoryReceipt.builder()
                .productId(product)
                .receiptQuantity(10)
                .receiptStatus("COMPLETED")
                .receiptDate(LocalDate.now())
                .build();

        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(inventoryReceiptRepository.save(any(InventoryReceipt.class))).thenReturn(receipt);
        when(inventoryRepository.findFirstByProductIdOrderByInventoryIdAsc(product)).thenReturn(Optional.of(inventory));

        // When
        InventoryReceiptResponse response = inventoryReceiptService.processReceipt(request);

        // Then
        verify(inventoryReceiptRepository).save(any(InventoryReceipt.class));
        verify(inventoryRepository).save(inventory);
        verify(inventoryHistoryRepository).save(any());
        assertThat(response).isNotNull();
    }

    @Test
    void processReceipt_입고처리_PENDING상태_재고변화없음() {
        // Given
        InventoryReceiptRequest request = InventoryReceiptRequest.of(
                100L,
                10,
                LocalDate.now(),
                "PENDING"
        );

        InventoryReceipt receipt = InventoryReceipt.builder()
                .productId(product)
                .receiptQuantity(10)
                .receiptStatus("PENDING")
                .receiptDate(LocalDate.now())
                .build();

        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(inventoryReceiptRepository.save(any(InventoryReceipt.class))).thenReturn(receipt);

        // When
        InventoryReceiptResponse response = inventoryReceiptService.processReceipt(request);

        // Then
        verify(inventoryReceiptRepository).save(any(InventoryReceipt.class));
        verify(inventoryRepository, never()).save(any());
        verify(inventoryHistoryRepository, never()).save(any());
        assertThat(response).isNotNull();
    }

    @Test
    void processReceipt_존재하지않는상품_예외발생() {
        // Given
        InventoryReceiptRequest request = InventoryReceiptRequest.of(
                999L,
                10,
                LocalDate.now(),
                "COMPLETED"
        );

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryReceiptService.processReceipt(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("존재하지 않는 상품입니다");

        verify(inventoryReceiptRepository, never()).save(any());
    }

    @Test
    void updateReceiptStatus_PENDING에서COMPLETED_재고증가() {
        // Given
        Long receiptId = 1L;
        String newStatus = "COMPLETED";

        InventoryReceipt receipt = InventoryReceipt.builder()
                .productId(product)
                .receiptQuantity(10)
                .receiptStatus("PENDING")
                .receiptDate(LocalDate.now())
                .build();

        when(inventoryReceiptRepository.findById(receiptId)).thenReturn(Optional.of(receipt));
        when(inventoryRepository.findFirstByProductIdOrderByInventoryIdAsc(product)).thenReturn(Optional.of(inventory));

        // When
        InventoryReceiptResponse response = inventoryReceiptService.updateReceiptStatus(receiptId, newStatus);

        // Then
        verify(inventoryRepository).save(inventory);
        verify(inventoryHistoryRepository).save(any());
        assertThat(response).isNotNull();
    }

    @Test
    void updateReceiptStatus_COMPLETED에서CANCELLED_재고감소() {
        // Given
        Long receiptId = 1L;
        String newStatus = "CANCELLED";

        InventoryReceipt receipt = InventoryReceipt.builder()
                .productId(product)
                .receiptQuantity(10)
                .receiptStatus("COMPLETED")
                .receiptDate(LocalDate.now())
                .build();

        when(inventoryReceiptRepository.findById(receiptId)).thenReturn(Optional.of(receipt));
        when(inventoryRepository.findFirstByProductIdOrderByInventoryIdAsc(product)).thenReturn(Optional.of(inventory));

        // When
        InventoryReceiptResponse response = inventoryReceiptService.updateReceiptStatus(receiptId, newStatus);

        // Then
        verify(inventoryRepository).save(inventory);
        verify(inventoryHistoryRepository).save(any());
        assertThat(response).isNotNull();
    }

    @Test
    void updateReceiptStatus_상태동일_변경없음() {
        // Given
        Long receiptId = 1L;
        String sameStatus = "COMPLETED";

        InventoryReceipt receipt = InventoryReceipt.builder()
                .productId(product)
                .receiptQuantity(10)
                .receiptStatus("COMPLETED")
                .receiptDate(LocalDate.now())
                .build();

        when(inventoryReceiptRepository.findById(receiptId)).thenReturn(Optional.of(receipt));

        // When
        InventoryReceiptResponse response = inventoryReceiptService.updateReceiptStatus(receiptId, sameStatus);

        // Then
        verify(inventoryRepository, never()).save(any());
        verify(inventoryHistoryRepository, never()).save(any());
        assertThat(response).isNotNull();
    }

    @Test
    void updateReceiptStatus_존재하지않는입고_예외발생() {
        // Given
        Long receiptId = 999L;
        String newStatus = "COMPLETED";

        when(inventoryReceiptRepository.findById(receiptId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryReceiptService.updateReceiptStatus(receiptId, newStatus))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("존재하지 않는 입고 정보입니다");

        verify(inventoryRepository, never()).save(any());
    }
}
