package com.bird.cos.domain.product;

import com.bird.cos.domain.common.CommonCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "PRODUCT_OPTION")
public class ProductOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long optionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_type", referencedColumnName = "code_id", nullable = false)
    private CommonCode optionTypeCode;

    @Column(name = "option_name", length = 100, nullable = false)
    private String optionName;

    @Column(name = "option_value", length = 100, nullable = false)
    private String optionValue;

    @Column(name = "additional_price", precision = 8, scale = 2)
    private BigDecimal additionalPrice = BigDecimal.ZERO;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
