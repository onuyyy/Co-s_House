// *8. 신규 생성 - CommonCode 엔티티용 JpaRepository
package com.bird.cos.repository;

import com.bird.cos.domain.common.CommonCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommonCodeRepository extends JpaRepository<CommonCode, String> {
}