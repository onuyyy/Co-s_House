package com.bird.cos.controller.cart;

import com.bird.cos.domain.user.User;
import com.bird.cos.dto.cart.*;
import com.bird.cos.repository.user.UserRepository;
import com.bird.cos.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<CartListResponse> add(@RequestBody AddToCartRequest req, Authentication auth) {
        User user = currentUser(auth);
        cartService.addCart(req, user);
        CartListResponse response = cartService.getCart(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<CartListResponse> list(Authentication auth) {
        User user = currentUser(auth);
        CartListResponse response = cartService.getCart(user);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{cartItemId}")
    public ResponseEntity<Void> updateQuantity(@PathVariable Long cartItemId,
                                               @RequestBody UpdateCartQuantityRequest req,
                                               Authentication auth) {
        User user = currentUser(auth);
        cartService.updateQuantity(cartItemId, user, req.quantity());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{cartItemId}/options")
    public ResponseEntity<Void> updateOptions(@PathVariable Long cartItemId,
                                              @RequestBody UpdateCartOptionsRequest req,
                                              Authentication auth) {
        User user = currentUser(auth);
        cartService.updateOptions(cartItemId, user, req.selectedOptions());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> deleteOne(@PathVariable Long cartItemId, Authentication auth) {
        User user = currentUser(auth);
        cartService.delete(List.of(cartItemId), user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMany(@RequestParam(name = "ids") String ids, Authentication auth) {
        User user = currentUser(auth);
        List<Long> list = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .toList();
        cartService.delete(list, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/checkout-info")
    public ResponseEntity<CheckoutInfoResponse> checkoutInfo(Authentication auth) {
        User user = currentUser(auth);
        var items = cartService.getCart(user).items().stream()
                .map(i -> new CheckoutInfoResponse.CheckoutItem(i.getProductId(), i.getTitle(), i.getQuantity()))
                .toList();
        return ResponseEntity.ok(new CheckoutInfoResponse(
                user.getUserName(),
                user.getUserPhone(),
                user.getUserAddress(),
                items
        ));
    }

    private User currentUser(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }
        return userRepository.findByUserEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    @PostMapping("/merge")
    public ResponseEntity<Void> merge(@RequestBody List<GuestCartItemRequest> req, Authentication auth) {
        User user = currentUser(auth);
        var items = req.stream()
                .map(r -> new CartService.GuestItem(r.productId(), r.quantity(), r.selectedOptions()))
                .toList();
        cartService.mergeGuestCart(items, user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
