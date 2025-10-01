package com.bird.cos.repository.user;

import com.bird.cos.domain.user.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {

    List<ShippingAddress> findAllByUser_UserId(Long userId);

    Optional<ShippingAddress> findByUser_UserIdAndIsDefaultTrue(Long userId);
}
