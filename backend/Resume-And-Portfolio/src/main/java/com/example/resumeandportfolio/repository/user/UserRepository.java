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

    // User ID로 사용자 찾기
    Optional<User> findByUserId(Long userId);

    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);

    // 이메일 중복 여부 확인
    boolean existsByEmail(String email);

    // 이메일로 삭제되지 않은 사용자 찾기
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    // User ID로 삭제되지 않은 사용자 찾기
    Optional<User> findByUserIdAndDeletedAtIsNull(Long userId);
}