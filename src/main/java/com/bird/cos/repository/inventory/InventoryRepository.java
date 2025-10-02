package com.bird.cos.repository.inventory;

import com.bird.cos.domain.inventory.Inventory;
import com.bird.cos.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {


    // 페이징된 재고 정보 조회
    @Query("SELECT i FROM Inventory i LEFT JOIN FETCH i.productId")
    Page<Inventory> findAllWithProduct(Pageable pageable);

    Optional<Inventory> findByProductId(Product productId);

    Optional<Inventory> findFirstByProductIdOrderByInventoryIdAsc(Product productId);

    @Query("SELECT i FROM Inventory i WHERE i.productId.productId IN :productIds")
    List<Inventory> findByProductIds(@Param("productIds") Collection<Long> productIds);

    // 상품 ID로 재고 검색 (페이징)
    @Query("SELECT i FROM Inventory i LEFT JOIN FETCH i.productId WHERE i.productId.productId = :productId")
    Page<Inventory> findByProductIdSearch(@Param("productId") Long productId, Pageable pageable);

    // 재고 상태별 검색 (페이징)
    @Query("SELECT i FROM Inventory i LEFT JOIN FETCH i.productId WHERE " +
           "(:status = 'DANGER' AND i.currentQuantity <= i.safetyQuantity) OR " +
           "(:status = 'WARNING' AND i.currentQuantity > i.safetyQuantity AND i.currentQuantity <= i.safetyQuantity * 2) OR " +
           "(:status = 'NORMAL' AND i.currentQuantity > i.safetyQuantity * 2)")
    Page<Inventory> findByInventoryStatus(@Param("status") String status, Pageable pageable);

    // 상품 ID와 재고 상태로 복합 검색 (페이징)
    @Query("SELECT i FROM Inventory i LEFT JOIN FETCH i.productId WHERE " +
           "i.productId.productId = :productId AND " +
           "((:status = 'DANGER' AND i.currentQuantity <= i.safetyQuantity) OR " +
           "(:status = 'WARNING' AND i.currentQuantity > i.safetyQuantity AND i.currentQuantity <= i.safetyQuantity * 2) OR " +
           "(:status = 'NORMAL' AND i.currentQuantity > i.safetyQuantity * 2))")
    Page<Inventory> findByProductIdAndInventoryStatus(@Param("productId") Long productId, @Param("status") String status, Pageable pageable);

    // 상품명으로 검색 (LIKE 검색)
    @Query("SELECT i FROM Inventory i LEFT JOIN FETCH i.productId WHERE " +
           "i.productId.productTitle LIKE CONCAT('%', :productName, '%')")
    Page<Inventory> findByProductNameSearch(@Param("productName") String productName, Pageable pageable);

    // 상품명과 재고 상태로 복합 검색
    @Query("SELECT i FROM Inventory i LEFT JOIN FETCH i.productId WHERE " +
           "i.productId.productTitle LIKE CONCAT('%', :productName, '%') AND " +
           "((:status = 'DANGER' AND i.currentQuantity <= i.safetyQuantity) OR " +
           "(:status = 'WARNING' AND i.currentQuantity > i.safetyQuantity AND i.currentQuantity <= i.safetyQuantity * 2) OR " +
           "(:status = 'NORMAL' AND i.currentQuantity > i.safetyQuantity * 2))")
    Page<Inventory> findByProductNameAndInventoryStatus(@Param("productName") String productName, @Param("status") String status, Pageable pageable);

    // 상품ID, 상품명, 재고상태 모두로 검색
    @Query("SELECT i FROM Inventory i LEFT JOIN FETCH i.productId WHERE " +
           "i.productId.productId = :productId AND " +
           "i.productId.productTitle LIKE CONCAT('%', :productName, '%') AND " +
           "((:status = 'DANGER' AND i.currentQuantity <= i.safetyQuantity) OR " +
           "(:status = 'WARNING' AND i.currentQuantity > i.safetyQuantity AND i.currentQuantity <= i.safetyQuantity * 2) OR " +
           "(:status = 'NORMAL' AND i.currentQuantity > i.safetyQuantity * 2))")
    Page<Inventory> findByProductIdAndProductNameAndInventoryStatus(@Param("productId") Long productId, @Param("productName") String productName, @Param("status") String status, Pageable pageable);

    // 상품ID와 상품명으로 검색
    @Query("SELECT i FROM Inventory i LEFT JOIN FETCH i.productId WHERE " +
           "i.productId.productId = :productId AND " +
           "i.productId.productTitle LIKE CONCAT('%', :productName, '%')")
    Page<Inventory> findByProductIdAndProductName(@Param("productId") Long productId, @Param("productName") String productName, Pageable pageable);
}
