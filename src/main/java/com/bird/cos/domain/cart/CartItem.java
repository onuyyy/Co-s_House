package com.bird.cos.domain.cart;

import com.bird.cos.domain.product.Product;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "CART_ITEM",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cartitem_cart_product", columnNames = {"cart_id", "product_id"})
        },
        indexes = {
                @Index(name = "idx_cartitem_cart", columnList = "cart_id"),
                @Index(name = "idx_cartitem_product", columnList = "product_id"),
                @Index(name = "idx_cartitem_created", columnList = "cart_item_created_at")
        })
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long cartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "selected_options", columnDefinition = "JSON")
    private String selectedOptions;

    @Column(name = "cart_item_created_at", insertable = false, updatable = false)
    private LocalDateTime cartItemCreatedAt;

    @Column(name = "cart_item_updated_at", insertable = false, updatable = false)
    private LocalDateTime cartItemUpdatedAt;

    public static CartItem of(Cart cart, Product product, int quantity, String selectedOptions) {
        CartItem i = new CartItem();
        i.cart = cart;
        i.product = product;
        i.quantity = Math.max(1, quantity);
        i.selectedOptions = selectedOptions;
        return i;
    }

    public void setCart(Cart cart) { this.cart = cart; }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
    }

    public void increaseQuantity(int delta) {
        int current = this.quantity == null ? 0 : this.quantity;
        this.quantity = Math.max(1, current + delta);
    }
}

