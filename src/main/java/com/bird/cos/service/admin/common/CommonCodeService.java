package com.bird.cos.service.admin.common;

import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.domain.common.CommonCodeGroup;
import com.bird.cos.dto.admin.CodeCreateRequest;
import com.bird.cos.dto.admin.CodeUpdateRequest;
import com.bird.cos.repository.common.CommonCodeGroupRepository;
import com.bird.cos.repository.common.CommonCodeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class CommonCodeService {

    private final CommonCodeRepository commonCodeRepository;
    private final CommonCodeGroupRepository commonCodeGroupRepository;

    @Transactional(readOnly = true)
    public List<CommonCodeGroup> getCommonCodeGroupList() {
        return commonCodeGroupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<CommonCode> getCommonCodeList(String groupId)
    {
        return commonCodeRepository.findByCommonCodeGroup_GroupId(groupId);
    }

    public void save(CodeCreateRequest.@Valid Code request) {

        CommonCodeGroup commonCodeGroup = commonCodeGroupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new RuntimeException("코드 그룹이 존재하지 않습니다."));

        CommonCode commonCode = CommonCode.builder()
                .codeId(request.getCodeId())
                .commonCodeGroup(commonCodeGroup)
                .codeName(request.getCodeName())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder())
                .isActive(request.getIsActive())
                .build();

         commonCodeRepository.save(commonCode);
    }

    public void save(CodeCreateRequest.@Valid CodeGroup request) {
        CommonCodeGroup commonCodeGroup = CommonCodeGroup.builder()
                .groupId(request.getGroupId())
                .groupName(request.getGroupName())
                .description(request.getDescription())
                .isActive(request.getIsActive())
                .build();

        commonCodeGroupRepository.save(commonCodeGroup);
    }

    public void deleteCode(String id) {
        CommonCode code = commonCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("삭제할 코드가 존재하지 않습니다."));
        commonCodeRepository.delete(code);
    }

    public void deleteCodeGroup(String id) {
        CommonCodeGroup group = commonCodeGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("삭제할 코드 그룹이 존재하지 않습니다."));
        commonCodeGroupRepository.delete(group);
    }

    public void updateCommonCode(CodeUpdateRequest.@Valid Code request) {
        // 기존 코드 엔티티 조회 먼저 해야 JPA가 인식 -> PK를 직접 할당해야 하는 전략이면
        CommonCode existingCode = commonCodeRepository.findById(request.getCodeId())
                .orElseThrow(() -> new RuntimeException("수정할 코드가 존재하지 않습니다."));

        CommonCodeGroup group = commonCodeGroupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new RuntimeException("코드 그룹이 존재하지 않습니다."));

        // 기존 엔티티의 값 업데이트
        existingCode.updateCode(
                request.getCodeName(),
                request.getDescription(),
                request.getSortOrder(),
                request.getIsActive(),
                group
        );

        commonCodeRepository.save(existingCode);
    }

    public void updateCommonCodeGroup(CodeUpdateRequest.@Valid CodeGroup request) {
        // 기존 그룹 엔티티 조회
        CommonCodeGroup existingGroup = commonCodeGroupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new RuntimeException("수정할 코드 그룹이 존재하지 않습니다."));

        // 기존 엔티티의 값 업데이트
        existingGroup.updateGroup(
                request.getGroupName(),
                request.getDescription(),
                request.getIsActive()
        );

        commonCodeGroupRepository.save(existingGroup);
    }

    public CommonCode getCommonCode(String codeId) {
        return commonCodeRepository.findById(codeId).orElseThrow(() -> new RuntimeException("코드가 존재하지 않습니다."));
    }

    public CommonCodeGroup getCommonCodeGroup(String groupId) {
        return commonCodeGroupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("코드 그룹이 존재하지 않습니다."));
    }
}
