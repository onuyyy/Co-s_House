package com.bird.cos.repository.cart;

import com.bird.cos.domain.cart.Cart;
import com.bird.cos.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartHeaderRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}

