package com.bird.cos.domain.cart;

import com.bird.cos.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "CART",
        indexes = {
                @Index(name = "idx_cart_user", columnList = "user_id"),
                @Index(name = "idx_cart_created", columnList = "cart_created_at")
        })
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "cart_created_at", insertable = false, updatable = false)
    private LocalDateTime cartCreatedAt;

    @Column(name = "cart_updated_at", insertable = false, updatable = false)
    private LocalDateTime cartUpdatedAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    public static Cart of(User user) {
        Cart c = new Cart();
        c.user = user;
        return c;
    }

    public void addItem(CartItem item) {
        if (!items.contains(item)) {
            items.add(item);
            item.setCart(this);
        }
    }
}

