package com.bird.cos.repository.inventory;

import com.bird.cos.domain.inventory.InventoryHistory;
import com.bird.cos.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, Long> {

    // 특정 상품의 히스토리 조회 (최신순)
    @Query("SELECT h FROM InventoryHistory h WHERE h.productId = :product ORDER BY h.changeDate DESC")
    List<InventoryHistory> findByProductIdOrderByChangeDateDesc(@Param("product") Product product);

    // 특정 상품의 히스토리 페이징 조회 (최신순)
    @Query("SELECT h FROM InventoryHistory h WHERE h.productId = :product ORDER BY h.changeDate DESC")
    Page<InventoryHistory> findByProductIdOrderByChangeDateDesc(@Param("product") Product product, Pageable pageable);

}