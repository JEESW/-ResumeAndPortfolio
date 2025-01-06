package com.example.resumeandportfolio.repository.user;

import com.example.resumeandportfolio.model.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * User's Repository
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);
}