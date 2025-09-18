package com.bird.cos.domain.product;

import com.bird.cos.domain.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CART")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "cart_quantity", nullable = false)
    private Integer cartQuantity = 1;

    @Column(name = "selected_options", columnDefinition = "JSON")
    private String selectedOptions;

    @Column(name = "cart_created_at", insertable = false, updatable = false)
    private LocalDateTime cartCreatedAt;

    @Column(name = "cart_updated_at", insertable = false, updatable = false)
    private LocalDateTime cartUpdatedAt;


}