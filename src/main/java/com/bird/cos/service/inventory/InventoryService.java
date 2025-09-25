package com.bird.cos.service.inventory;

import com.bird.cos.domain.inventory.Inventory;
import com.bird.cos.dto.admin.InventoryManageResponse;
import com.bird.cos.exception.BusinessException;
import com.bird.cos.exception.ErrorCode;
import com.bird.cos.repository.inventory.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    // 모든 재고 정보 조회
    @Transactional(readOnly = true)
    public List<InventoryManageResponse> getAllInventory() {
        try {
            List<Inventory> inventoryList = inventoryRepository.findAllWithProduct();
            return inventoryList.stream()
                    .map(InventoryManageResponse::from)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw BusinessException.of(ErrorCode.INVALID_OPERATION, "재고 목록을 조회할 수 없습니다");
        }
    }

    public long getTotalCount() {
        return inventoryRepository.count();
    }
}
