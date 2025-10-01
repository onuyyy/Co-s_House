package com.bird.cos.service.inventory;

import com.bird.cos.domain.inventory.Inventory;
import com.bird.cos.domain.inventory.InventoryHistory;
import com.bird.cos.domain.inventory.InventoryOutbound;
import com.bird.cos.domain.order.Order;
import com.bird.cos.domain.order.OrderItem;
import com.bird.cos.domain.product.Product;
import com.bird.cos.exception.BusinessException;
import com.bird.cos.exception.ErrorCode;
import com.bird.cos.repository.inventory.InventoryHistoryRepository;
import com.bird.cos.repository.inventory.InventoryOutboundRepository;
import com.bird.cos.repository.inventory.InventoryRepository;
import com.bird.cos.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryOutboundService {

    private static final String OUTBOUND_STATUS_COMPLETED = "OUTBOUND_COMPLETED";
    private static final String OUTBOUND_STATUS_PENDING = "OUTBOUND_PENDING";
    private static final String OUTBOUND_STATUS_CANCELLED = "OUTBOUND_CANCELLED";

    private final InventoryOutboundRepository inventoryOutboundRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;
    private final OrderRepository  orderRepository;

    /**
    * 주문번호를 받아서 재고 출고 처리
    * @param orderId 주문 ID
    */
    @Transactional
    public void processOutboundForOrder(Long orderId){
        // 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> BusinessException.of(ErrorCode.INVALID_OPERATION, "존재하지 않는 주문입니다."));

        // 주문 아이템들 순회하면서 각각 출고 처리
        for(OrderItem orderItem : order.getOrderItems()){
            Product product = orderItem.getProduct();
            Integer quantity = orderItem.getQuantity();

            // 재고 먼저 검증
            validateInventoryAvailable(product, quantity);

            //출고 레코드 생성
            InventoryOutbound outbound = InventoryOutbound.builder()
                    .orderId(order)
                    .productId(product)
                    .outboundQuantity(quantity)
                    .outboundStatus(OUTBOUND_STATUS_COMPLETED)
                    .outboundDate(LocalDate.now())
                    .build();

            InventoryOutbound savedOutbound = inventoryOutboundRepository.save(outbound);

            //재고 수량 감소
            updateInventoryQuantity(product, -quantity, savedOutbound);
        }
    }

    /**
    * 주문 취소/환불 시 재고 복구 처리
    * @param orderId 주문 ID
    */
    @Transactional
    public void processInboundForOrder(Long orderId){
        // 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> BusinessException.of(ErrorCode.INVALID_OPERATION, "존재하지 않는 주문입니다."));

        // 주문 아이템들 순회하면서 각각 입고 처리
        for(OrderItem orderItem : order.getOrderItems()){
            Product product = orderItem.getProduct();
            Integer quantity = orderItem.getQuantity();

            //입고 레코드 생성 (취소)
            InventoryOutbound outbound = InventoryOutbound.builder()
                    .orderId(order)
                    .productId(product)
                    .outboundQuantity(-quantity)
                    .outboundStatus(OUTBOUND_STATUS_CANCELLED)
                    .outboundDate(LocalDate.now())
                    .build();

            InventoryOutbound savedOutbound = inventoryOutboundRepository.save(outbound);

            //재고 수량 증가
            updateInventoryQuantity(product, quantity, savedOutbound);
        }
    }

    /**
    * 재고 가용 여부 검증
    */
    private void validateInventoryAvailable(Product product, Integer quantity) {
        Inventory inventory = inventoryRepository.findFirstByProductIdOrderByInventoryIdAsc(product)
                .orElseThrow(() -> BusinessException.of(ErrorCode.INVALID_OPERATION, "해당 상품의 재고 정보를 찾을 수 없습니다."));

        Integer currentQuantity = inventory.getCurrentQuantity();
        if (currentQuantity < quantity) {
            throw BusinessException.of(ErrorCode.INVALID_OPERATION,
                    String.format("재고가 부족합니다. 상품 ID: %d, 현재재고: %d, 출고수량: %d",
                            product.getProductId(), currentQuantity, quantity));
        }
    }

    /**
    * 재고 수량 업데이트 및 히스토리 기록
    */
    private void updateInventoryQuantity(Product product, Integer changeQuantity, InventoryOutbound outbound) {
        //재고 조회
        Inventory inventory = inventoryRepository.findFirstByProductIdOrderByInventoryIdAsc(product)
                .orElseThrow(()->BusinessException.of(ErrorCode.INVALID_OPERATION, "해당 상품의 재고 정보를 찾을 수 없습니다."));

        //현재 재고 확인
        Integer beforeQuantity = inventory.getCurrentQuantity();
        int afterQuantity = beforeQuantity + changeQuantity;

        //재고  업데이트
        inventory.updateCurrentQuantity(afterQuantity);
        inventoryRepository.save(inventory);

        //출고 히스토리 기록
        recordInventoryHistory(inventory, changeQuantity, afterQuantity, product, outbound);
    }

    /**
    * 출고 히스토리 기록
    */
    private void recordInventoryHistory(Inventory inventory, Integer changeQuantity, int afterQuantity, Product product, InventoryOutbound outbound) {
        InventoryHistory history = InventoryHistory.builder()
                .productId(product)
                .inventoryId(inventory)
                .outboundId(outbound)
                .changeQuantity(changeQuantity)
                .afterQuantity(afterQuantity)
                .build();

        inventoryHistoryRepository.save(history);
    }

}
