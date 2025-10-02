package com.bird.cos.repository.user;

import com.bird.cos.domain.user.UserGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserGradeRepository extends JpaRepository<UserGrade, Integer> {

    Optional<UserGrade> findTopByUser_UserIdOrderByGradePeriodEndDesc(Long userId);
}
