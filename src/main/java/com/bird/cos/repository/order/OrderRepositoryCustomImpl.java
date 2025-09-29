package com.bird.cos.repository.order;

import com.bird.cos.domain.order.Order;
import com.bird.cos.domain.order.QOrder;
import com.bird.cos.domain.order.QOrderItem;
import com.bird.cos.domain.product.QProduct;
import com.bird.cos.domain.product.QProductOption;
import com.bird.cos.dto.mypage.MyOrderRequest;
import com.bird.cos.dto.mypage.SearchDate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Order> searchOrders(Long userId, MyOrderRequest request, Pageable pageable) {
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;
        QProduct product = QProduct.product;
        QProductOption productOption = QProductOption.productOption;

        // 전체 데이터 조회 쿼리
        JPAQuery<Order> query = queryFactory
                .selectFrom(order)
                .distinct()
                .leftJoin(order.orderItems, orderItem).fetchJoin()
                .leftJoin(orderItem.product, product).fetchJoin()
                .leftJoin(orderItem.productOption, productOption).fetchJoin()
                .leftJoin(orderItem.deliveryStatusCode).fetchJoin()
                .leftJoin(order.orderStatusCode).fetchJoin()
                .where(
                        userIdEq(userId),
                        searchDateCondition(request != null ? request.getSearchDate() : null),
                        orderStatusEq(request != null ? request.getOrderStatus() : null),
                        searchValueContains(request != null ? request.getSearchValue() : null)
                )
                .orderBy(order.orderDate.desc());

        // 전체 개수 조회
        long total = query.fetch().size();

        // 페이징 적용
        List<Order> orders = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(orders, pageable, total);
    }

    /**
     * 사용자 ID 조건
     */
    private BooleanExpression userIdEq(Long userId) {
        return userId != null ? QOrder.order.user.userId.eq(userId) : null;
    }

    /**
     * 기간 검색 조건
     */
    private BooleanExpression searchDateCondition(SearchDate searchDate) {
        if (searchDate == null || searchDate == SearchDate.ALL) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = switch (searchDate) {
            case MONTH_3 -> now.minusMonths(3);
            case MONTH_6 -> now.minusMonths(6);
            case MONTH_12 -> now.minusMonths(12);
            default -> null;
        };

        return startDate != null ? QOrder.order.orderDate.goe(startDate) : null;
    }

    /**
     * 주문 상태 조건
     */
    private BooleanExpression orderStatusEq(String orderStatus) {
        if (orderStatus == null || orderStatus.isEmpty() || orderStatus.equals("ALL")) {
            return null;
        }
        return QOrder.order.orderStatusCode.codeId.eq(orderStatus);
    }

    /**
     * 검색어 조건 (상품명, 옵션명, 브랜드명)
     */
    private BooleanExpression searchValueContains(String searchValue) {
        if (searchValue == null || searchValue.trim().isEmpty()) {
            return null;
        }

        QOrderItem orderItem = QOrderItem.orderItem;
        QProduct product = QProduct.product;
        QProductOption productOption = QProductOption.productOption;

        // 상품명 또는 옵션명에 검색어가 포함되어 있는 경우
        BooleanExpression productNameContains = product.productTitle.containsIgnoreCase(searchValue);
        BooleanExpression optionValueContains = productOption.optionValue.containsIgnoreCase(searchValue);

        return productNameContains.or(optionValueContains);
    }
}
