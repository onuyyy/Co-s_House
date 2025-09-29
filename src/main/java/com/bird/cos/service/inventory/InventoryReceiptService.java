package com.bird.cos.service.inventory;

import com.bird.cos.domain.inventory.InventoryReceipt;
import com.bird.cos.domain.product.Product;
import com.bird.cos.dto.admin.InventoryReceiptRequest;
import com.bird.cos.dto.admin.InventoryReceiptResponse;
import com.bird.cos.exception.BusinessException;
import com.bird.cos.exception.ErrorCode;
import com.bird.cos.exception.GlobalExceptionHandler;
import com.bird.cos.repository.inventory.InventoryReceiptRepository;
import com.bird.cos.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryReceiptService {

    private final InventoryReceiptRepository inventoryReceiptRepository;
    private final ProductRepository productRepository;

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

        return InventoryReceiptResponse.from(inventoryReceipt);
    }

}