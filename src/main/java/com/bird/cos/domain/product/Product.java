package com.bird.cos.domain.product;

import com.bird.cos.domain.brand.Brand;
import com.bird.cos.domain.common.CommonCode;
import com.bird.cos.domain.delivery.DeliveryInfo;
import com.bird.cos.domain.inventory.Inventory;
import com.bird.cos.domain.inventory.InventoryHistory;
import com.bird.cos.domain.inventory.InventoryOutbound;
import com.bird.cos.domain.inventory.InventoryReceipt;
import com.bird.cos.domain.order.OrderItem;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "PRODUCT")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_title", length = 255, nullable = false)
    private String productTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_category_id", nullable = false)
    private ProductCategory productCategory;

    @Column(name = "main_image_url", length = 500, nullable = false)
    private String mainImageUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "original_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal originalPrice;

    @Column(name = "sale_price", precision = 10, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "coupon_price", precision = 10, scale = 2)
    private BigDecimal couponPrice;

    @Column(name = "discount_rate", precision = 5, scale = 2)
    private BigDecimal discountRate;

    @Column(name = "is_free_shipping")
    private Boolean isFreeShipping = false;

    @Column(name = "is_today_deal")
    private Boolean isTodayDeal = false;

    @Column(name = "is_cohouse_only")
    private Boolean isCohouseOnly = false;

    @Column(name = "product_color", length = 50)
    private String productColor;

    @Column(name = "material", length = 100)
    private String material;

    @Column(name = "capacity", length = 50)
    private String capacity;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "sales_count")
    private Long salesCount = 0L;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "bookmark_count")
    private Integer bookmarkCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_status", referencedColumnName = "code_id", nullable = false)
    private CommonCode productStatusCode;

    @Column(name = "product_created_at", insertable = false, updatable = false)
    private LocalDateTime productCreatedAt;

    @Column(name = "product_updated_at", insertable = false, updatable = false)
    private LocalDateTime productUpdatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductOption> options = new ArrayList<>();

    // 상품 정보 업데이트 메서드
    public void update(com.bird.cos.dto.admin.ProductUpdateRequest request) {
        if (request.getProductTitle() != null && !request.getProductTitle().trim().isEmpty()) {
            this.productTitle = request.getProductTitle().trim();
        }
        if (request.getDescription() != null) {
            this.description = request.getDescription().trim();
        }
        if (request.getOriginalPrice() != null) {
            this.originalPrice = request.getOriginalPrice();
        }
        if (request.getSalePrice() != null) {
            this.salePrice = request.getSalePrice();
        }
        if (request.getCouponPrice() != null) {
            this.couponPrice = request.getCouponPrice();
        }
        if (request.getDiscountRate() != null) {
            this.discountRate = request.getDiscountRate();
        }
        if (request.getProductColor() != null) {
            this.productColor = request.getProductColor().trim();
        }
        if (request.getMaterial() != null) {
            this.material = request.getMaterial().trim();
        }
        if (request.getCapacity() != null) {
            this.capacity = request.getCapacity().trim();
        }
        if (request.getStockQuantity() != null) {
            this.stockQuantity = request.getStockQuantity();
        }
        if (request.getIsFreeShipping() != null) {
            this.isFreeShipping = request.getIsFreeShipping();
        }
        if (request.getIsTodayDeal() != null) {
            this.isTodayDeal = request.getIsTodayDeal();
        }
        if (request.getIsCohouseOnly() != null) {
            this.isCohouseOnly = request.getIsCohouseOnly();
        }
        if (request.getMainImageUrl() != null) {
            this.mainImageUrl = request.getMainImageUrl().trim();
        }
    }
}
