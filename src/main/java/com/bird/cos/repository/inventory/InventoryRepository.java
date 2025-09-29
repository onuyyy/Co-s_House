package com.bird.cos.repository.inventory;

import com.bird.cos.domain.inventory.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {


    // 페이징된 재고 정보 조회
    @Query("SELECT i FROM Inventory i LEFT JOIN FETCH i.productId")
    Page<Inventory> findAllWithProduct(Pageable pageable);

    // 특정 상품의 재고 정보 조회
    @Query("SELECT i FROM Inventory i LEFT JOIN FETCH i.productId WHERE i.productId.productId = :productId")
    Optional<Inventory> findByProductIdWithProduct(@Param("productId") Long productId);

}