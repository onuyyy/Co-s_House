package com.bird.cos.domain.product;

import com.bird.cos.domain.common.CommonCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "PRODUCT_CATEGORY")
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", length = 100, nullable = false)
    private String categoryName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    @Setter
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
        child.setParentCategory(this);
    }
}