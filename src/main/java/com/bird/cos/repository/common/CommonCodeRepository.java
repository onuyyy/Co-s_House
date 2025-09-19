package com.bird.cos.repository.common;

import com.bird.cos.domain.common.CommonCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommonCodeRepository extends JpaRepository<CommonCode, String> {
    List<CommonCode> findByCommonCodeGroup_GroupId(String groupId);
}
