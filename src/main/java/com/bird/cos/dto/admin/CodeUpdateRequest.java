package com.bird.cos.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class CodeUpdateRequest {

    @Data
    public static class CodeGroup {

        @NotBlank(message = "그룹 ID는 필수입니다.")
        private String groupId;

        @NotBlank(message = "그룹 이름은 필수입니다.")
        @Size(max = 100, message = "그룹 이름은 최대 100자까지 가능합니다.")
        private String groupName;

        @Size(max = 255, message = "설명은 최대 255자까지 가능합니다.")
        private String description;

        @NotNull(message = "활성 여부를 선택해주세요.")
        private Boolean isActive;
    }

    @Data
    public static class Code {

        @NotBlank(message = "코드 ID는 필수입니다.")
        private String codeId;

        @NotBlank(message = "그룹 ID는 필수입니다.")
        private String groupId;

        @NotBlank(message = "코드 이름은 필수입니다.")
        @Size(max = 100, message = "코드 이름은 최대 100자까지 가능합니다.")
        private String codeName;

        @Size(max = 255, message = "설명은 최대 255자까지 가능합니다.")
        private String description;

        @NotNull(message = "정렬 순서를 입력해주세요.")
        private Integer sortOrder;

        @NotNull(message = "활성 여부를 선택해주세요.")
        private Boolean isActive;
    }
}
