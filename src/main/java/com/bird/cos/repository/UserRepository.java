// *7. 신규 생성 - User 엔티티용 JpaRepository
package com.bird.cos.repository;

import com.bird.cos.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}