package com.bird.cos.repository.cart;

import com.bird.cos.domain.cart.Cart;
import com.bird.cos.domain.cart.CartItem;
import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findAllByCart_User(User user);
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    Optional<CartItem> findByCartAndProductAndSelectedOptions(Cart cart, Product product, String selectedOptions);
    Optional<CartItem> findByCart_CartIdAndCart_User(Long cartId, User user); // header scope
    Optional<CartItem> findByCartItemIdAndCart_User(Long cartItemId, User user);
    List<CartItem> findAllByCartItemIdInAndCart_User(List<Long> ids, User user);
}
