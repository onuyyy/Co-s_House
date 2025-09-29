package com.bird.cos.repository.order;

import com.bird.cos.domain.order.Order;
import com.bird.cos.dto.mypage.MyOrderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryCustom {
    Page<Order> searchOrders(Long userId, MyOrderRequest myOrderRequest, Pageable pageable);
}
