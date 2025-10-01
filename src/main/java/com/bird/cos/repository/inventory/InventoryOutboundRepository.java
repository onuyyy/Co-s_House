package com.bird.cos.repository.inventory;

import com.bird.cos.domain.inventory.InventoryOutbound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryOutboundRepository extends JpaRepository<InventoryOutbound, Long> {

    // 출고 처리를 위한 기본 CRUD 사용
    // save() - 출고 데이터 저장
    // findById() - 출고 정보 조회

}
