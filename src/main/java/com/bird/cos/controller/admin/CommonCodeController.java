package com.bird.cos.controller.admin;

import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.domain.common.CommonCodeGroup;
import com.bird.cos.dto.admin.CodeCreateRequest;
import com.bird.cos.service.admin.common.CommonCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/admin/common-code")
public class CommonCodeController {

    private final CommonCodeService commonCodeService;

    @GetMapping
    public String commonCodePage()
    {
        return "admin/common-code/code-list";
    }

    @ResponseBody
    @GetMapping("/groups")
    public List<CommonCodeGroup> getCodeGroups()
    {
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

    @PostMapping("/code")
    public String createCommonCode(
            @Valid CodeCreateRequest.Code request, BindingResult bindingResult
    )
    {
        if (bindingResult.hasErrors()) {
            log.info("createProduct binding error: {}", Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
           // todo : 화면 새로고침 안 하고 어떻게 모달창 유지하지?
        }

        commonCodeService.save(request);

        return "redirect:admin/common-code/code-list";
    }

    @PostMapping("/group")
    public String createCommonGroupCode(
            @Valid CodeCreateRequest.CodeGroup request, BindingResult bindingResult
    )
    {
        if (bindingResult.hasErrors()) {
            log.info("createCommonGroupCode binding error: {}", Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
            // todo : 화면 새로고침 안 하고 어떻게 모달창 유지하지?
        }

        commonCodeService.save(request);

        return "redirect:admin/common-code/code-list";
    }
}
