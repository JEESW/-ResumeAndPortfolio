package com.example.resumeandportfolio.repository.user;

import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.model.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User's Repository Test
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("이메일로 사용자 찾기 - 성공")
    void findByEmailSuccess() {
        // Given: 테스트 데이터 삽입
        User user = User.builder()
            .email("test@example.com")
            .password("encoded_password")
            .nickname("Tester")
            .role(Role.VISITOR)
            .build();
        userRepository.save(user);

        // When: 이메일로 사용자 조회
        Optional<User> result = userRepository.findByEmail("test@example.com");

        // Then: 결과 검증
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        assertThat(result.get().getNickname()).isEqualTo("Tester");
        assertThat(result.get().getRole()).isEqualTo(Role.VISITOR);
    }

    @Test
    @DisplayName("이메일로 사용자 찾기 - 실패")
    void findByEmailFailure() {
        // When: 존재하지 않는 이메일로 사용자 조회
        Optional<User> result = userRepository.findByEmail("notfound@example.com");

        // Then: 결과 검증
        assertThat(result).isNotPresent();
    }

    @Test
    @DisplayName("이메일 중복 여부 확인 - 중복된 이메일 존재")
    void existsByEmailTrue() {
        // Given: 테스트 데이터 삽입
        User user = User.builder()
            .email("duplicate@example.com")
            .password("encoded_password")
            .nickname("DuplicateUser")
            .role(Role.VISITOR)
            .build();
        userRepository.save(user);

        // When: 중복 이메일 확인
        boolean result = userRepository.existsByEmail("duplicate@example.com");

        // Then: 결과 검증
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("이메일 중복 여부 확인 - 중복된 이메일 없음")
    void existsByEmailFalse() {
        // When: 중복 이메일 확인
        boolean result = userRepository.existsByEmail("notfound@example.com");

        // Then: 결과 검증
        assertThat(result).isFalse();
    }
}