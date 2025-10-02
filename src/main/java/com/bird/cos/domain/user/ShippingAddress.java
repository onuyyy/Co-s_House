package com.bird.cos.domain.user;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ShippingAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipping_address_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_phone")
    private String recipientPhone;

    private String postcode;

    private String address;

    @Column(name = "detail_address")
    private String detailAddress;

    @Column(name = "is_default")
    @ColumnDefault("false")
    private Boolean isDefault;

    public void setUser(User user) {
        this.user = user;
    }

    public void setAsDefault() {
        this.isDefault = true;
    }

    public void unsetAsDefault() {
        this.isDefault = false;
    }

    public void update(String recipientName, String recipientPhone, String postcode, String address, String detailAddress, Boolean isDefault) {
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.postcode = postcode;
        this.address = address;
        this.detailAddress = detailAddress;
        this.isDefault = isDefault;
    }
}
