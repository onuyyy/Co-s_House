package com.bird.cos.domain.proudct;

import com.bird.cos.domain.common.CommonCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PRODUCT_CATEGORY")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", length = 100, nullable = false)
    private String categoryName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ProductCategory parentCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_type", referencedColumnName = "code_id", nullable = false)
    private CommonCode categoryTypeCode;

    @Column(name = "level")
    private Integer level = 1;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "category_created_at", insertable = false, updatable = false)
    private LocalDateTime categoryCreatedAt;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductCategory> childCategories = new ArrayList<>();

    public void addChildCategory(ProductCategory child) {
        childCategories.add(child);
        // parentCategory 설정은 Builder로 생성 시에 처리하도록 변경 필요
        // 또는 별도의 비즈니스 메서드로 처리
    }


}