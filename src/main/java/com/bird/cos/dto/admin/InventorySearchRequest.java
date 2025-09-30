package com.bird.cos.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventorySearchRequest {

    private Long productId;          // 상품 ID로 검색
    private String productName;      // 상품명으로 검색
    private String inventoryStatus;  // 재고 상태로 검색 (NORMAL, WARNING, DANGER)

    // 검색 조건이 있는지 확인하는 메서드
    public boolean hasSearchCondition() {
        return productId != null ||
               (productName != null && !productName.trim().isEmpty()) ||
               (inventoryStatus != null && !inventoryStatus.trim().isEmpty());
    }
}