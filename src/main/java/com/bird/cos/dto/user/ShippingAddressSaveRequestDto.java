package com.bird.cos.dto.user;

import com.bird.cos.domain.user.ShippingAddress;
import com.bird.cos.domain.user.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShippingAddressSaveRequestDto {

    @NotBlank(message = "받는 분은 필수입니다.")
    @Size(max = 50, message = "받는 분 이름은 50자 이하여야 합니다.")
    private String recipientName;

    @NotBlank(message = "연락처는 필수입니다.")
    @Pattern(regexp = "^0\\d{1,2}-?\\d{3,4}-?\\d{4}$", message = "연락처 형식을 다시 확인해주세요.")
    private String recipientPhone;

    @NotBlank(message = "우편번호는 필수입니다.")
    @Size(max = 10, message = "우편번호는 10자 이하여야 합니다.")
    private String postcode;

    @NotBlank(message = "주소는 필수입니다.")
    @Size(max = 255, message = "주소는 255자 이하여야 합니다.")
    private String address;

    @NotBlank(message = "상세주소는 필수입니다.")
    @Size(max = 255, message = "상세주소는 255자 이하여야 합니다.")
    private String detailAddress;

    private Boolean isDefault = Boolean.FALSE;

    public ShippingAddress toEntity(User user) {
        return ShippingAddress.builder()
                .user(user)
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .postcode(postcode)
                .address(address)
                .detailAddress(detailAddress)
                .isDefault(Boolean.TRUE.equals(isDefault))
                .build();
    }
}
