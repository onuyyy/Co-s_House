package com.bird.cos.service.inventory;

import com.bird.cos.domain.inventory.Inventory;
import com.bird.cos.dto.admin.InventoryManageResponse;
import com.bird.cos.dto.admin.InventorySearchRequest;
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

    // 재고 검색 (페이징)
    @Transactional(readOnly = true)
    public Page<InventoryManageResponse> searchInventory(InventorySearchRequest searchRequest, Pageable pageable) {
        try {
            // 검색 조건이 없으면 전체 조회
            if (!searchRequest.hasSearchCondition()) {
                return getAllInventoryPage(pageable);
            }

            Page<Inventory> inventoryPage;

            Long productId = searchRequest.getProductId();
            String productName = searchRequest.getProductName();
            String inventoryStatus = searchRequest.getInventoryStatus();

            boolean hasProductId = productId != null;
            boolean hasProductName = productName != null && !productName.trim().isEmpty();
            boolean hasStatus = inventoryStatus != null && !inventoryStatus.trim().isEmpty();

            // 모든 조건이 있는 경우
            if (hasProductId && hasProductName && hasStatus) {
                inventoryPage = inventoryRepository.findByProductIdAndProductNameAndInventoryStatus(
                    productId, productName, inventoryStatus, pageable);
            }
            // 상품ID + 상품명
            else if (hasProductId && hasProductName) {
                inventoryPage = inventoryRepository.findByProductIdAndProductName(
                    productId, productName, pageable);
            }
            // 상품ID + 재고상태
            else if (hasProductId && hasStatus) {
                inventoryPage = inventoryRepository.findByProductIdAndInventoryStatus(
                    productId, inventoryStatus, pageable);
            }
            // 상품명 + 재고상태
            else if (hasProductName && hasStatus) {
                inventoryPage = inventoryRepository.findByProductNameAndInventoryStatus(
                    productName, inventoryStatus, pageable);
            }
            // 상품ID만
            else if (hasProductId) {
                inventoryPage = inventoryRepository.findByProductIdSearch(productId, pageable);
            }
            // 상품명만
            else if (hasProductName) {
                inventoryPage = inventoryRepository.findByProductNameSearch(productName, pageable);
            }
            // 재고상태만
            else if (hasStatus) {
                inventoryPage = inventoryRepository.findByInventoryStatus(inventoryStatus, pageable);
            }
            // 검색 조건이 없는 경우 (혹시나 해서)
            else {
                inventoryPage = inventoryRepository.findAllWithProduct(pageable);
            }

            return inventoryPage.map(InventoryManageResponse::from);

        } catch (Exception e) {
            throw BusinessException.of(ErrorCode.INVALID_OPERATION, "재고 검색 중 오류가 발생했습니다.");
        }
    }
}
