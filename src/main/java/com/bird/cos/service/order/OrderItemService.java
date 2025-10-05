package com.bird.cos.service.order;

import com.bird.cos.domain.order.OrderItem;
import com.bird.cos.dto.order.OrderItemResponse;
import com.bird.cos.repository.order.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    public List<OrderItemResponse> getMyOrderItems(Long userId) {
        List<OrderItem> orderItems = orderItemRepository.findAllByOrder_User_UserId(userId);

        // 중복 제거: productId 기준으로 distinct
        return new ArrayList<>(orderItems.stream()
                .collect(Collectors.toMap(
                        o -> o.getProduct().getProductId(),  // key: productId
                        o -> OrderItemResponse.builder()      // value: OrderItemResponse
                                .productId(o.getProduct().getProductId())
                                .productTitle(o.getProduct().getProductTitle())
                                .mainImageUrl(o.getProduct().getMainImageUrl())
                                .originalPrice(o.getProduct().getOriginalPrice())
                                .build(),
                        (existing, replacement) -> existing  // 중복 시 기존 것 유지
                ))
                .values());
    }

}
