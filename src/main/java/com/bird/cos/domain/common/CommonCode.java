package com.bird.cos.domain.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "COMMON_CODE")
public class CommonCode {

    @Id
    @Column(name = "code_id", length = 50)
    private String codeId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", referencedColumnName = "group_id", nullable = false)
    private CommonCodeGroup commonCodeGroup;

    @Column(name = "code_name", length = 100, nullable = false)
    private String codeName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public void updateCode(String codeName, String description, Integer sortOrder, Boolean isActive, CommonCodeGroup commonCodeGroup) {
        this.codeName = codeName;
        this.description = description;
        this.sortOrder = sortOrder;
        this.isActive = isActive;
        this.commonCodeGroup = commonCodeGroup;
    }

}