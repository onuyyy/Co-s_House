package com.bird.cos.controller.admin;

import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.domain.common.CommonCodeGroup;
import com.bird.cos.dto.admin.CodeCreateRequest;
import com.bird.cos.dto.admin.CodeUpdateRequest;
import com.bird.cos.service.admin.common.CommonCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // Ajax용 JSON API 엔드포인트
    @PostMapping("/groups")
    @ResponseBody
    public ResponseEntity<?> createCommonGroupCode(
            @Valid CodeCreateRequest.CodeGroup request, BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "입력값이 올바르지 않습니다.",
                "errors", errors
            ));
        }

        try {
            commonCodeService.save(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "코드 그룹이 성공적으로 저장되었습니다."
            ));
        } catch (Exception e) {
            log.error("코드 그룹 저장 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "코드 그룹 저장에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/codes")
    @ResponseBody
    public ResponseEntity<?> createCommonCode(
            @Valid CodeCreateRequest.Code request, BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "입력값이 올바르지 않습니다.",
                "errors", errors
            ));
        }

        try {
            commonCodeService.save(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "코드가 성공적으로 저장되었습니다."
            ));
        } catch (Exception e) {
            log.error("코드 저장 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "코드 저장에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/code/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteCommonCode(
            @PathVariable String id
    ) {
        try {
            commonCodeService.deleteCode(id);
            return ResponseEntity.ok().body(Map.of(
                    "success", "true",
                    "message", "코드가 삭제되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.ok().body(Map.of(
                    "success", "false",
                    "message", "코드 삭제가 실패되었습니다."
            ));
        }

    }

    @GetMapping("/group/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteCommonCodeGroup(
            @PathVariable String id
    ) {
        try {
            commonCodeService.deleteCodeGroup(id);
            return ResponseEntity.ok().body(Map.of(
                    "success", "true",
                    "message", "코드 그룹이 삭제되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.ok().body(Map.of(
                    "success", "false",
                    "message", "코드 그룹 삭제가 실패되었습니다."
            ));
        }
    }

    @GetMapping("/codes/{codeId}")
    @ResponseBody
    public CommonCode getCommonCode(
            @PathVariable String codeId
    ) {
        return commonCodeService.getCommonCode(codeId);
    }

    @GetMapping("/groups/{groupId}")
    @ResponseBody
    public CommonCodeGroup getCommonCodeGroup(
            @PathVariable String groupId
    ) {
        return commonCodeService.getCommonCodeGroup(groupId);
    }

    @PostMapping("/codes/update")
    @ResponseBody
    public ResponseEntity<?> updateCommonCode(
            @Valid CodeUpdateRequest.Code request, BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "입력값이 올바르지 않습니다.",
                "errors", errors
            ));
        }

        try {
            commonCodeService.updateCommonCode(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "코드가 성공적으로 수정되었습니다."
            ));
        } catch (Exception e) {
            log.error("코드 수정 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "코드 수정에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/groups/update")
    @ResponseBody
    public ResponseEntity<?> updateCommonCodeGroup(
            @Valid CodeUpdateRequest.CodeGroup request, BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "입력값이 올바르지 않습니다.",
                "errors", errors
            ));
        }

        try {
            commonCodeService.updateCommonCodeGroup(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "코드 그룹이 성공적으로 수정되었습니다."
            ));
        } catch (Exception e) {
            log.error("코드 그룹 수정 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "코드 그룹 수정에 실패했습니다: " + e.getMessage()
            ));
        }
    }

}
