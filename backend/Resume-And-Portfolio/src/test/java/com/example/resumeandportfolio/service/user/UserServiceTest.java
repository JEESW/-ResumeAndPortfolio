package com.example.resumeandportfolio.service.user;

import com.example.resumeandportfolio.exception.CustomException;
import com.example.resumeandportfolio.exception.ErrorCode;
import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * User's Service Test
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "encoded_password", "Tester", null);
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccessTest() {
        // Given: Repository와 PasswordEncoder 동작 정의
        when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("raw_password", "encoded_password"))
            .thenReturn(true);

        // When: 서비스 호출
        User result = userService.login("test@example.com", "raw_password");

        // Then: 결과 검증
        assertEquals(testUser, result);

        // Verify: Mock 메서드 호출 검증
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("raw_password", "encoded_password");
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 사용자 없음")
    void loginFailureUserNotFoundTest() {
        // Given: Repository에서 사용자 반환 없음
        when(userRepository.findByEmail("notfound@example.com"))
            .thenReturn(Optional.empty());

        // When & Then: 예외 검증
        CustomException exception = assertThrows(CustomException.class, () ->
            userService.login("notfound@example.com", "any_password")
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository, times(1)).findByEmail("notfound@example.com");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 잘못된 비밀번호")
    void loginFailureInvalidPasswordTest() {
        // Given: Repository 동작 정의 및 잘못된 비밀번호 설정
        when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong_password", "encoded_password"))
            .thenReturn(false);

        // When & Then: 예외 검증
        CustomException exception = assertThrows(CustomException.class, () ->
            userService.login("test@example.com", "wrong_password")
        );

        assertEquals(ErrorCode.INVALID_PASSWORD, exception.getErrorCode());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("wrong_password", "encoded_password");
    }
}