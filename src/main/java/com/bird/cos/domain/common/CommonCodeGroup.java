package com.bird.cos.domain.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "COMMON_CODE_GROUP")
public class CommonCodeGroup {

    @Id
    @Column(name = "group_id", length = 50)
    private String groupId;

    @Column(name = "group_name", length = 100, nullable = false)
    private String groupName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "commonCodeGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CommonCode> commonCodes = new ArrayList<>();

    public void updateGroup(String groupName, String description, Boolean isActive) {
        this.groupName = groupName;
        this.description = description;
        this.isActive = isActive;
    }

}