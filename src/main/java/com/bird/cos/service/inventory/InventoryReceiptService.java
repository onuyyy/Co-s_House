package com.bird.cos.service.inventory;

import com.bird.cos.domain.inventory.Inventory;
import com.bird.cos.domain.inventory.InventoryHistory;
import com.bird.cos.domain.inventory.InventoryReceipt;
import com.bird.cos.domain.product.Product;
import com.bird.cos.dto.admin.InventoryReceiptRequest;
import com.bird.cos.dto.admin.InventoryReceiptResponse;
import com.bird.cos.dto.admin.InventoryHistoryResponse;
import com.bird.cos.exception.BusinessException;
import com.bird.cos.exception.ErrorCode;
import com.bird.cos.repository.inventory.InventoryReceiptRepository;
import com.bird.cos.repository.inventory.InventoryRepository;
import com.bird.cos.repository.inventory.InventoryHistoryRepository;
import com.bird.cos.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryReceiptService {

    private static final String RECEIPT_STATUS_COMPLETED = "COMPLETED";
    private static final String RECEIPT_STATUS_PENDING = "PENDING";
    private static final String RECEIPT_STATUS_CANCELLED = "CANCELLED";

    private final InventoryReceiptRepository inventoryReceiptRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;

    // 입고 처리
    @Transactional
    public InventoryReceiptResponse processReceipt(InventoryReceiptRequest request) {
        //상품 존재 여부 확인
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> BusinessException.of(ErrorCode.INVALID_OPERATION, "존재하지 않는 상품입니다."));

        //입고 레코드 생성 및 저장
        InventoryReceipt receipt = InventoryReceipt.builder()
                .productId(product)
                .receiptQuantity(request.getReceiptQuantity())
                .receiptStatus(request.getReceiptStatus())
                .receiptDate(request.getReceiptDate())
                .build();

        InventoryReceipt inventoryReceipt = inventoryReceiptRepository.save(receipt);

        // 입고 상태가 COMPLETED일 때만 재고 수량 증가
        if (RECEIPT_STATUS_COMPLETED.equals(request.getReceiptStatus())) {
            updateInventoryQuantity(product, request.getReceiptQuantity(), inventoryReceipt);
            log.info("재고 업데이트 완료 - 상품ID: {}, 입고수량: {}", product.getProductId(), request.getReceiptQuantity());
        } else {
            log.info("재고 업데이트 생략 - 상태: {}", request.getReceiptStatus());
        }

        log.info("입고 처리 완료 - 입고ID: {}", inventoryReceipt.getReceiptId());
        return InventoryReceiptResponse.from(inventoryReceipt);
    }

    private void updateInventoryQuantity(Product product, Integer quantity, InventoryReceipt receipt) {
        // 해당 상품의 재고 조회
        Inventory inventory = inventoryRepository.findFirstByProductIdOrderByInventoryIdAsc(product)
                .orElseThrow(() -> BusinessException.of(ErrorCode.INVALID_OPERATION, "해당 상품의 재고 정보를 찾을 수 없습니다."));

        // 현재 재고에 입고 수량 추가
        Integer beforeQuantity = inventory.getCurrentQuantity();
        Integer afterQuantity = beforeQuantity + quantity;

        // 재고 업데이트
        inventory.updateCurrentQuantity(afterQuantity);
        inventoryRepository.save(inventory);

        // 입고 히스토리 기록
        recordInventoryHistory(inventory, quantity, afterQuantity, product, receipt);
    }

    // 입고 히스토리 기록
    private void recordInventoryHistory(Inventory inventory, Integer changeQuantity,
                                      Integer afterQuantity,
                                      Product product, InventoryReceipt receipt) {
        InventoryHistory history = InventoryHistory.builder()
                .productId(product)
                .inventoryId(inventory)
                .receiptId(receipt)
                .changeQuantity(changeQuantity)
                .afterQuantity(afterQuantity)
                .build();

        inventoryHistoryRepository.save(history);
    }

    // 해당 상품의 재고 이력 조회 (페이징)
    public Page<InventoryHistoryResponse> getInventoryHistoryPage(Long productId, Pageable pageable) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.INVALID_OPERATION, "존재하지 않는 상품입니다."));

        Page<InventoryHistory> historyPage = inventoryHistoryRepository.findByProductIdOrderByChangeDateDesc(product, pageable);

        return historyPage.map(InventoryHistoryResponse::from);
    }

    // 입고 목록 조회 (페이징)
    public Page<InventoryReceiptResponse> getAllReceiptsPage(Pageable pageable) {
        Page<InventoryReceipt> receiptPage = inventoryReceiptRepository.findAllWithProduct(pageable);
        return receiptPage.map(InventoryReceiptResponse::from);
    }

    // 입고 상태 변경
    @Transactional
    public InventoryReceiptResponse updateReceiptStatus(Long receiptId, String newStatus) {
        InventoryReceipt receipt = inventoryReceiptRepository.findById(receiptId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.INVALID_OPERATION, "존재하지 않는 입고 정보입니다."));

        String previousStatus = receipt.getReceiptStatus();

        // 상태가 같으면 처리하지 않음
        if (previousStatus.equals(newStatus)) {
            log.info("입고 상태 변경 없음 - 입고ID: {}, 상태: {}", receiptId, newStatus);
            return InventoryReceiptResponse.from(receipt);
        }

        // 상태 변경
        receipt.updateReceiptStatus(newStatus);

        // 재고 수량 조정
        adjustInventoryForStatusChange(receipt, previousStatus, newStatus);

        log.info("입고 상태 변경 완료 - 입고ID: {}, {} -> {}", receiptId, previousStatus, newStatus);
        return InventoryReceiptResponse.from(receipt);
    }

    private void adjustInventoryForStatusChange(InventoryReceipt receipt, String previousStatus, String newStatus) {
        Product product = receipt.getProductId();
        Integer quantity = receipt.getReceiptQuantity();

        // PENDING -> COMPLETED: 재고 증가
        if (RECEIPT_STATUS_PENDING.equals(previousStatus) && RECEIPT_STATUS_COMPLETED.equals(newStatus)) {
            updateInventoryQuantity(product, quantity, receipt);
            log.info("재고 증가 - 상품ID: {}, 수량: +{}", product.getProductId(), quantity);
        }
        // COMPLETED -> PENDING: 재고 감소 (롤백)
        else if (RECEIPT_STATUS_COMPLETED.equals(previousStatus) && RECEIPT_STATUS_PENDING.equals(newStatus)) {
            updateInventoryQuantity(product, -quantity, receipt);
            log.info("재고 감소 (롤백) - 상품ID: {}, 수량: -{}", product.getProductId(), quantity);
        }
        // COMPLETED -> CANCELLED: 재고 감소 (취소)
        else if (RECEIPT_STATUS_COMPLETED.equals(previousStatus) && RECEIPT_STATUS_CANCELLED.equals(newStatus)) {
            updateInventoryQuantity(product, -quantity, receipt);
            log.info("재고 감소 (취소) - 상품ID: {}, 수량: -{}", product.getProductId(), quantity);
        }
        // PENDING -> CANCELLED: 재고 변화 없음
        else if (RECEIPT_STATUS_PENDING.equals(previousStatus) && RECEIPT_STATUS_CANCELLED.equals(newStatus)) {
            log.info("재고 변화 없음 (대기->취소) - 상품ID: {}", product.getProductId());
        }
        // CANCELLED -> COMPLETED: 재고 증가
        else if (RECEIPT_STATUS_CANCELLED.equals(previousStatus) && RECEIPT_STATUS_COMPLETED.equals(newStatus)) {
            updateInventoryQuantity(product, quantity, receipt);
            log.info("재고 증가 (취소->완료) - 상품ID: {}, 수량: +{}", product.getProductId(), quantity);
        }
        // CANCELLED -> PENDING: 재고 변화 없음
        else if (RECEIPT_STATUS_CANCELLED.equals(previousStatus) && RECEIPT_STATUS_PENDING.equals(newStatus)) {
            log.info("재고 변화 없음 (취소->대기) - 상품ID: {}", product.getProductId());
        }
    }

}