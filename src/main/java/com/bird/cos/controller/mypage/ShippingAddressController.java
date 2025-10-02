package com.bird.cos.controller.mypage;

import com.bird.cos.dto.user.ShippingAddressDto;
import com.bird.cos.dto.user.ShippingAddressSaveRequestDto;
import com.bird.cos.service.user.ShippingAddressService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage/shipping-addresses")
public class ShippingAddressController {

    private final ShippingAddressService shippingAddressService;

    private Long getUserIdFromSession(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            // Consider using a proper exception handler
            throw new SecurityException("로그인이 필요합니다.");
        }
        return userId;
    }

    @GetMapping
    public ResponseEntity<List<ShippingAddressDto>> getShippingAddresses(HttpSession session) {
        Long userId = getUserIdFromSession(session);
        List<ShippingAddressDto> addresses = shippingAddressService.findAllByUserId(userId);
        return ResponseEntity.ok(addresses);
    }

    @PostMapping
    public ResponseEntity<Long> saveShippingAddress(@Valid @RequestBody ShippingAddressSaveRequestDto requestDto, HttpSession session) {
        Long userId = getUserIdFromSession(session);
        Long savedId = shippingAddressService.save(userId, requestDto);
        return new ResponseEntity<>(savedId, HttpStatus.CREATED);
    }

    @PutMapping("/{addressId}/default")
    public ResponseEntity<Void> setAsDefault(@PathVariable Long addressId, HttpSession session) {
        Long userId = getUserIdFromSession(session);
        shippingAddressService.setAsDefault(userId, addressId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<Void> updateShippingAddress(@PathVariable Long addressId,
                                                      @Valid @RequestBody ShippingAddressSaveRequestDto requestDto,
                                                      HttpSession session) {
        Long userId = getUserIdFromSession(session);
        shippingAddressService.update(userId, addressId, requestDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteShippingAddress(@PathVariable Long addressId, HttpSession session) {
        Long userId = getUserIdFromSession(session);
        shippingAddressService.delete(userId, addressId);
        return ResponseEntity.noContent().build();
    }
}
