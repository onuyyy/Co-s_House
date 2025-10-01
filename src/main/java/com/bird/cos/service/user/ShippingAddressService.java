package com.bird.cos.service.user;

import com.bird.cos.domain.user.ShippingAddress;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.user.ShippingAddressDto;
import com.bird.cos.dto.user.ShippingAddressSaveRequestDto;
import com.bird.cos.repository.user.ShippingAddressRepository;
import com.bird.cos.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShippingAddressService {

    private final ShippingAddressRepository shippingAddressRepository;
    private final UserRepository userRepository;

    public List<ShippingAddressDto> findAllByUserId(Long userId) {
        return shippingAddressRepository.findAllByUser_UserId(userId).stream()
                .map(ShippingAddressDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Long save(Long userId, ShippingAddressSaveRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. id=" + userId));

        if (Boolean.TRUE.equals(requestDto.getIsDefault())) {
            // Unset the old default address
            shippingAddressRepository.findByUser_UserIdAndIsDefaultTrue(userId)
                    .ifPresent(ShippingAddress::unsetAsDefault);
        }

        ShippingAddress shippingAddress = requestDto.toEntity(user);
        return shippingAddressRepository.save(shippingAddress).getId();
    }

    @Transactional
    public void setAsDefault(Long userId, Long addressId) {
        // Unset the old default address
        shippingAddressRepository.findByUser_UserIdAndIsDefaultTrue(userId)
                .ifPresent(ShippingAddress::unsetAsDefault);

        ShippingAddress shippingAddress = shippingAddressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주소가 없습니다. id=" + addressId));

        // Check authorization
        if (!shippingAddress.getUser().getUserId().equals(userId)) {
            throw new SecurityException("주소에 대한 권한이 없습니다.");
        }

        shippingAddress.setAsDefault();
    }

    @Transactional
    public void update(Long userId, Long addressId, ShippingAddressSaveRequestDto requestDto) {
        ShippingAddress shippingAddress = shippingAddressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주소가 없습니다. id=" + addressId));

        // 권한 확인
        if (!shippingAddress.getUser().getUserId().equals(userId)) {
            throw new SecurityException("주소에 대한 권한이 없습니다.");
        }

        // 만약 새로운 주소를 기본 배송지로 설정한다면, 기존의 기본 배송지를 해제합니다.
        if (Boolean.TRUE.equals(requestDto.getIsDefault())) {
            shippingAddressRepository.findByUser_UserIdAndIsDefaultTrue(userId)
                    .ifPresent(oldDefault -> {
                        if (!oldDefault.getId().equals(addressId)) {
                            oldDefault.unsetAsDefault();
                        }
                    });
        }

        // 주소 정보 업데이트
        shippingAddress.update(
                requestDto.getRecipientName(),
                requestDto.getRecipientPhone(),
                requestDto.getPostcode(),
                requestDto.getAddress(),
                requestDto.getDetailAddress(),
                Boolean.TRUE.equals(requestDto.getIsDefault())
        );
    }

    @Transactional
    public void delete(Long userId, Long addressId) {
        ShippingAddress shippingAddress = shippingAddressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주소가 없습니다. id=" + addressId));

        // Check authorization
        if (!shippingAddress.getUser().getUserId().equals(userId)) {
            throw new SecurityException("주소에 대한 권한이 없습니다.");
        }

        shippingAddressRepository.delete(shippingAddress);
    }
}
