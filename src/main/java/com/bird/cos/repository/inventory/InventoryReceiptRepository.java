package com.bird.cos.repository.inventory;

import com.bird.cos.domain.inventory.InventoryReceipt;
import com.bird.cos.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryReceiptRepository extends JpaRepository<InventoryReceipt, Long> {

    // 입고처리를 위한 기본 CRUD만 사용
    // save() - 입고 데이터 저장
    // findById() - 입고 정보 조회
}