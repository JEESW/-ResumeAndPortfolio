package com.example.resumeandportfolio.service.user;

import com.example.resumeandportfolio.exception.CustomException;
import com.example.resumeandportfolio.exception.ErrorCode;
import com.example.resumeandportfolio.model.dto.user.UserLoginResponse;
import com.example.resumeandportfolio.model.dto.user.UserRegisterRequest;
import com.example.resumeandportfolio.model.dto.user.UserRegisterResponse;
import com.example.resumeandportfolio.model.dto.user.UserUpdateRequest;
import com.example.resumeandportfolio.model.dto.user.UserUpdateResponse;
import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.model.enums.Role;
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
        // Given
        User testUserWithId = new User(
            "test@example.com",
            "encoded_password",
            "Tester",
            Role.VISITOR
        ) {
            @Override
            public Long getUserId() {
                return 1L;
            }
        };

        when(userRepository.findByEmailAndDeletedAtIsNull("test@example.com"))
            .thenReturn(Optional.of(testUserWithId));
        when(passwordEncoder.matches("raw_password", "encoded_password"))
            .thenReturn(true);

        // When
        UserLoginResponse response = userService.login("test@example.com", "raw_password");

        // Then
        assertEquals(1L, response.userId());
        assertEquals("test@example.com", response.email());
        assertEquals("Tester", response.nickname());

        // Verify
        verify(userRepository, times(1)).findByEmailAndDeletedAtIsNull("test@example.com");
        verify(passwordEncoder, times(1)).matches("raw_password", "encoded_password");
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 사용자 없음")
    void loginFailureUserNotFoundTest() {
        // Given: Repository에서 사용자 반환 없음
        when(userRepository.findByEmailAndDeletedAtIsNull("notfound@example.com"))
            .thenReturn(Optional.empty());

        // When & Then: 예외 검증
        CustomException exception = assertThrows(CustomException.class, () ->
            userService.login("notfound@example.com", "any_password")
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository, times(1)).findByEmailAndDeletedAtIsNull("notfound@example.com");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 잘못된 비밀번호")
    void loginFailureInvalidPasswordTest() {
        // Given: Repository 동작 정의 및 잘못된 비밀번호 설정
        when(userRepository.findByEmailAndDeletedAtIsNull("test@example.com"))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong_password", "encoded_password"))
            .thenReturn(false);

        // When & Then: 예외 검증
        CustomException exception = assertThrows(CustomException.class, () ->
            userService.login("test@example.com", "wrong_password")
        );

        assertEquals(ErrorCode.INVALID_PASSWORD, exception.getErrorCode());
        verify(userRepository, times(1)).findByEmailAndDeletedAtIsNull("test@example.com");
        verify(passwordEncoder, times(1)).matches("wrong_password", "encoded_password");
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void registerSuccessTest() {
        // Given: 회원가입 요청 데이터와 이미 사용하지 않은 이메일
        UserRegisterRequest request = new UserRegisterRequest("new@example.com", "password123", "NewUser");

        when(userRepository.existsByEmail("new@example.com"))
            .thenReturn(false);
        when(passwordEncoder.encode("password123"))
            .thenReturn("encoded_password");
        User savedUser = User.builder()
            .email("new@example.com")
            .password("encoded_password")
            .nickname("NewUser")
            .role(Role.VISITOR)
            .build();
        when(userRepository.save(any(User.class)))
            .thenReturn(savedUser);

        // When: 회원가입 서비스 호출
        UserRegisterResponse response = userService.register(request);

        // Then: 결과 검증
        assertEquals("new@example.com", response.email());
        assertEquals("NewUser", response.nickname());
        assertEquals(Role.VISITOR, response.role());

        // Verify: Mock 메서드 호출 검증
        verify(userRepository, times(1)).existsByEmail("new@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 이메일 중복")
    void registerFailureEmailAlreadyExistsTest() {
        // Given: 중복된 이메일이 존재하는 경우
        UserRegisterRequest request = new UserRegisterRequest("duplicate@example.com", "password123", "DuplicateUser");

        when(userRepository.existsByEmail("duplicate@example.com"))
            .thenReturn(true);

        // When & Then: 예외 검증
        CustomException exception = assertThrows(CustomException.class, () ->
            userService.register(request)
        );

        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, exception.getErrorCode());

        // Verify: Mock 메서드 호출 검증
        verify(userRepository, times(1)).existsByEmail("duplicate@example.com");
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    @DisplayName("회원 수정 성공 테스트")
    void updateUserSuccessTest() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest(
            "new_nickname",
            "current_password",
            "new_password123"
        );

        User testUserWithId = new User(
            "test@example.com",
            "encoded_password",
            "Tester",
            Role.VISITOR
        ) {
            @Override
            public Long getUserId() {
                return 1L;
            }
        };

        when(userRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testUserWithId));
        when(passwordEncoder.matches("current_password", "encoded_password")).thenReturn(true);
        when(passwordEncoder.encode("new_password123")).thenReturn("encoded_new_password");

        User updatedUser = new User(
            "test@example.com",
            "encoded_new_password",
            "new_nickname",
            Role.VISITOR
        ) {
            @Override
            public Long getUserId() {
                return 1L;
            }
        };

        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        UserUpdateResponse response = userService.updateUser(1L, request);

        // Then
        assertEquals(1L, response.userId());
        assertEquals("test@example.com", response.email());
        assertEquals("new_nickname", response.nickname());
        assertEquals(Role.VISITOR, response.role());

        // Verify
        verify(userRepository, times(1)).findByUserIdAndDeletedAtIsNull(1L);
        verify(passwordEncoder, times(1)).matches("current_password", "encoded_password");
        verify(passwordEncoder, times(1)).encode("new_password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("회원 수정 실패 테스트 - 잘못된 현재 비밀번호")
    void updateUserFailureInvalidPasswordTest() {
        // Given: 수정 요청 데이터와 잘못된 현재 비밀번호 설정
        UserUpdateRequest request = new UserUpdateRequest(
            "new_nickname",
            "wrong_password",
            "new_password123"
        );

        User existingUser = new User(
            "test@example.com",
            "encoded_password",
            "old_nickname",
            Role.VISITOR
        ) {
            @Override
            public Long getUserId() {
                return 1L;
            }
        };

        when(userRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrong_password", "encoded_password")).thenReturn(false);

        // When & Then: 예외 검증
        CustomException exception = assertThrows(CustomException.class, () ->
            userService.updateUser(1L, request)
        );

        assertEquals(ErrorCode.INVALID_PASSWORD, exception.getErrorCode());

        // Verify: Mock 메서드 호출 검증
        verify(userRepository, times(1)).findByUserIdAndDeletedAtIsNull(1L);
        verify(passwordEncoder, times(1)).matches("wrong_password", "encoded_password");
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    @DisplayName("회원 수정 실패 테스트 - 사용자 없음")
    void updateUserFailureUserNotFoundTest() {
        // Given: 없는 사용자에 대한 요청
        UserUpdateRequest request = new UserUpdateRequest(
            "new_nickname",
            "current_password",
            "new_password123"
        );

        when(userRepository.findByUserIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        // When & Then: 예외 검증
        CustomException exception = assertThrows(CustomException.class, () ->
            userService.updateUser(1L, request)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());

        // Verify: Mock 메서드 호출 검증
        verify(userRepository, times(1)).findByUserIdAndDeletedAtIsNull(1L);
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, times(0)).save(any(User.class));
    }
}