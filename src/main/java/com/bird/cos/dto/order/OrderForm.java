package com.bird.cos.dto.order;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class OrderForm {
    private List<OrderRequest> orderItems = new ArrayList<>(); // List<OrderRequest> 담기
}
