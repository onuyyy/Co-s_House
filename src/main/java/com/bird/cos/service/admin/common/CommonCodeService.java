package com.bird.cos.service.admin.common;

import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.domain.common.CommonCodeGroup;
import com.bird.cos.repository.common.CommonCodeGroupRepository;
import com.bird.cos.repository.common.CommonCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CommonCodeService {

    private final CommonCodeRepository commonCodeRepository;
    private final CommonCodeGroupRepository commonCodeGroupRepository;

    public List<CommonCodeGroup> getCommonCodeGroupList() {
        return commonCodeGroupRepository.findAll();
    }

    public List<CommonCode> getCommonCodeList(String groupId) {
        return commonCodeRepository.findByCommonCodeGroup_GroupId(groupId);
    }
}
