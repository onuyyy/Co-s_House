package com.bird.cos.repository.common;

import com.bird.cos.domain.common.CommonCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommonCodeRepository extends JpaRepository<CommonCode, String> {
}
