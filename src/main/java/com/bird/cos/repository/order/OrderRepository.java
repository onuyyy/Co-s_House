package com.bird.cos.repository.order;

import com.bird.cos.domain.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long>, OrderRepositoryCustom {

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product p " +
            "LEFT JOIN FETCH oi.productOption po " +
            "LEFT JOIN FETCH oi.deliveryStatusCode " +
            "LEFT JOIN FETCH o.orderStatusCode " +
            "WHERE o.user.userId = :userId " +
            "ORDER BY o.orderDate DESC")
    List<Order> findByUserIdWithDetails(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o " +
            "WHERE o.user.userId = :userId " +
            "ORDER BY o.orderDate DESC")
    Page<Order> findByUserIdOrderByOrderDateDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product p " +
            "LEFT JOIN FETCH oi.productOption po " +
            "LEFT JOIN FETCH oi.deliveryStatusCode " +
            "LEFT JOIN FETCH o.orderStatusCode " +
            "WHERE o.orderId IN :orderIds")
    List<Order> findByOrderIdsWithDetails(@Param("orderIds") List<Long> orderIds);
}
