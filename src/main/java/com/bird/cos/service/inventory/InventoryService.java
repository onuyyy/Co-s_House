package com.bird.cos.service.inventory;

import com.bird.cos.domain.inventory.Inventory;
import com.bird.cos.dto.admin.InventoryManageResponse;
import com.bird.cos.exception.BusinessException;
import com.bird.cos.exception.ErrorCode;
import com.bird.cos.repository.inventory.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    // 페이징된 재고 정보 조회
    @Transactional(readOnly = true)
    public Page<InventoryManageResponse> getAllInventoryPage(Pageable pageable) {
        try {
            Page<Inventory> inventoryPage = inventoryRepository.findAllWithProduct(pageable);
            return inventoryPage.map(InventoryManageResponse::from);
        } catch (Exception e) {
            throw BusinessException.of(ErrorCode.INVALID_OPERATION, "재고 목록을 조회할 수 없습니다");
        }
    }

    public long getTotalCount() {
        return inventoryRepository.count();
    }

    @Transactional(readOnly = true)
    public InventoryManageResponse getInventoryById(Long inventoryId) {

        try {
            Inventory inventory = inventoryRepository.findById(inventoryId)
                    .orElseThrow(()-> BusinessException.of(ErrorCode.INVALID_OPERATION, "재고 정보를 찾을 수 없습니다."));
            return InventoryManageResponse.from(inventory);
        }catch (Exception e){
            throw BusinessException.of(ErrorCode.INVALID_OPERATION,"재고 정보를 조회할 수 없습니다.");
        }
    }
}
