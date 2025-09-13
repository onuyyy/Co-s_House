package com.bird.cos.domain.user;

import com.bird.cos.domain.admin.CustomerService;
import com.bird.cos.domain.log.UserActivityLog;
import com.bird.cos.domain.order.Order;
import com.bird.cos.domain.order.OrderStatusHistory;
import com.bird.cos.domain.post.Comment;
import com.bird.cos.domain.post.Like;
import com.bird.cos.domain.post.Post;
import com.bird.cos.domain.post.Scrap;
import com.bird.cos.domain.proudct.Answer;
import com.bird.cos.domain.proudct.Cart;
import com.bird.cos.domain.proudct.Question;
import com.bird.cos.domain.proudct.Review;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "USER")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_email", length = 255, unique = true, nullable = false)
    private String userEmail;

    @Column(name = "user_password", length = 255)
    private String userPassword;

    @Column(name = "user_nickname", length = 20, unique = true, nullable = false)
    private String userNickname;

    @Column(name = "user_name", length = 50, nullable = false)
    private String userName;

    @Column(name = "user_address", columnDefinition = "TEXT")
    private String userAddress;

    @Column(name = "user_phone", length = 20)
    private String userPhone;

    @Column(name = "social_provider", length = 20)
    private String socialProvider;

    @Column(name = "social_id", length = 255)
    private String socialId;

    @Column(name = "terms_agreed")
    private Boolean termsAgreed = false;

    @Column(name = "user_created_at", insertable = false, updatable = false)
    private LocalDateTime userCreatedAt;

    @Column(name = "user_updated_at", insertable = false, updatable = false)
    private LocalDateTime userUpdatedAt;

}