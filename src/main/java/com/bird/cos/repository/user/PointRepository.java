package com.bird.cos.repository.user;

import com.bird.cos.domain.user.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {

    boolean existsByUser_UserIdAndPointDescription(Long userId, String pointDescription);
}
