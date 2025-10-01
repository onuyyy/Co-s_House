package com.bird.cos.dto.user;

import com.bird.cos.domain.user.ShippingAddress;
import lombok.Getter;

@Getter
public class ShippingAddressDto {

    private Long id;
    private String recipientName;
    private String recipientPhone;
    private String postcode;
    private String address;
    private String detailAddress;
    private Boolean isDefault;

    public ShippingAddressDto(ShippingAddress entity) {
        this.id = entity.getId();
        this.recipientName = entity.getRecipientName();
        this.recipientPhone = entity.getRecipientPhone();
        this.postcode = entity.getPostcode();
        this.address = entity.getAddress();
        this.detailAddress = entity.getDetailAddress();
        this.isDefault = entity.getIsDefault();
    }
}
