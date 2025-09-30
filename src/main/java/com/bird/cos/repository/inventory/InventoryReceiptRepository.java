package com.bird.cos.repository.inventory;

import com.bird.cos.domain.inventory.InventoryReceipt;
import com.bird.cos.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryReceiptRepository extends JpaRepository<InventoryReceipt, Long> {

    // 입고처리를 위한 기본 CRUD만 사용
    // save() - 입고 데이터 저장
    // findById() - 입고 정보 조회

    // 페이징된 입고 목록 조회 (상품 정보 포함)
    @Query("SELECT ir FROM InventoryReceipt ir LEFT JOIN FETCH ir.productId ORDER BY ir.receiptDate DESC, ir.receiptId DESC")
    Page<InventoryReceipt> findAllWithProduct(Pageable pageable);
}