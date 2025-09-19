package com.bird.cos.controller.admin;

import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.domain.common.CommonCodeGroup;
import com.bird.cos.service.admin.common.CommonCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/admin/common-code")
public class CommonCodeController {

    private final CommonCodeService commonCodeService;

    // 코드 메인 페이지 접속 시
    @GetMapping
    public String commonCodePage()
    {
        return "admin/common-code/code-list";
    }

    @ResponseBody
    @GetMapping("/groups")
    public List<CommonCodeGroup> getCodeGroups() {
        return commonCodeService.getCommonCodeGroupList();
    }

    @ResponseBody
    @GetMapping("/common-codes/{groupId}/child")
    public List<CommonCode> getCommonCodeList(
            @PathVariable String groupId
    )
    {
        return commonCodeService.getCommonCodeList(groupId);
    }
}
