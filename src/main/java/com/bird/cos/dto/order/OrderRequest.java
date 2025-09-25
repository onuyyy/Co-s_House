package com.bird.cos.dto.order;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class OrderRequest {

    private Long productId;
    private Long productOptionId;
    private Integer quantity;
    private BigDecimal price;
}
