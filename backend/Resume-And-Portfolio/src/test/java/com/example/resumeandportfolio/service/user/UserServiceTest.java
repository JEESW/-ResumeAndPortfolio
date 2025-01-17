package com.example.resumeandportfolio.service.user;

import com.example.resumeandportfolio.exception.CustomException;
import com.example.resumeandportfolio.exception.ErrorCode;
import com.example.resumeandportfolio.model.dto.user.PasswordResetConfirmDto;
import com.example.resumeandportfolio.model.dto.user.PasswordResetRequestDto;
import com.example.resumeandportfolio.model.dto.user.UserLoginResponse;
import com.example.resumeandportfolio.model.dto.user.UserRegisterRequest;
import com.example.resumeandportfolio.model.dto.user.UserRegisterResponse;
import com.example.resumeandportfolio.model.dto.user.UserUpdateRequest;
import com.example.resumeandportfolio.model.dto.user.UserUpdateResponse;
import com.example.resumeandportfolio.model.dto.user.VerificationTokenDto;
import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.model.enums.Role;
import com.example.resumeandportfolio.repository.user.UserRepository;
import com.example.resumeandportfolio.util.mail.MailUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private MailUtil mailUtil;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        testUser = User.builder()
            .email("test@example.com")
            .password("encoded_password")
            .nickname("Tester")
            .role(Role.VISITOR)
            .build();

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(objectMapper.writeValueAsString(any(VerificationTokenDto.class))).thenReturn("mockedTokenData");
        lenient().when(valueOperations.get(anyString())).thenReturn("mockedTokenData");

        ReflectionTestUtils.setField(userService, "expirationHours", 24);
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
    @DisplayName("회원가입 이메일 인증 요청 성공 테스트")
    void initiateRegistrationSuccessTest() {
        // Given
        UserRegisterRequest request = new UserRegisterRequest(
            "newuser@example.com", "password123", "password123", "NewUser"
        );
        when(userRepository.existsByEmail(request.email())).thenReturn(false);

        // When
        userService.initiateRegistration(request);

        // Then
        verify(userRepository, times(1)).existsByEmail(request.email());
        verify(valueOperations, times(1))
            .set(anyString(), anyString(), eq(Duration.ofHours(24)));
        verify(mailUtil, times(1)).sendVerificationMail(eq(request.email()), anyString());
    }

    @Test
    @DisplayName("회원가입 이메일 인증 요청 실패 테스트 - 이메일 중복")
    void initiateRegistrationFailureEmailExistsTest() {
        // Given
        UserRegisterRequest request = new UserRegisterRequest(
            "duplicate@example.com", "password123", "password123", "DuplicateUser"
        );
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
            userService.initiateRegistration(request)
        );
        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, exception.getErrorCode());
        verify(userRepository, times(1)).existsByEmail(request.email());
    }

    @Test
    @DisplayName("인증 이메일 재전송 성공 테스트")
    void resendVerificationEmailSuccessTest() {
        // Given
        when(userRepository.existsByEmail("resend@example.com")).thenReturn(true);

        // When
        userService.resendVerificationEmail("resend@example.com");

        // Then
        verify(userRepository, times(1)).existsByEmail("resend@example.com");
        verify(valueOperations, times(1))
            .set(anyString(), anyString(), eq(Duration.ofHours(24)));
        verify(mailUtil, times(1)).sendVerificationMail(eq("resend@example.com"), anyString());
    }

    @Test
    @DisplayName("인증 이메일 재전송 실패 테스트 - 사용자 없음")
    void resendVerificationEmailFailureUserNotFoundTest() {
        // Given
        when(userRepository.existsByEmail("notfound@example.com")).thenReturn(false);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
            userService.resendVerificationEmail("notfound@example.com")
        );
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository, times(1)).existsByEmail("notfound@example.com");
    }

    @Test
    @DisplayName("회원가입 완료 성공 테스트")
    void completeRegistrationSuccessTest() throws Exception {
        // Given
        String token = "validToken";
        VerificationTokenDto tokenDto = new VerificationTokenDto(
            token, "complete@example.com", LocalDateTime.now().plusHours(1)
        );
        String tokenData = objectMapper.writeValueAsString(tokenDto);

        when(valueOperations.get("verification:token:" + token)).thenReturn(tokenData);
        when(objectMapper.readValue(tokenData, VerificationTokenDto.class)).thenReturn(tokenDto);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");

        User savedUser = User.builder()
            .email("complete@example.com")
            .password("encoded_password")
            .nickname("CompleteUser")
            .role(Role.VISITOR)
            .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserRegisterResponse response = userService.completeRegistration(
            token, "password123", "CompleteUser"
        );

        // Then
        assertEquals("complete@example.com", response.email());
        assertEquals("CompleteUser", response.nickname());
        assertEquals(Role.VISITOR, response.role());

        verify(valueOperations, times(1)).get("verification:token:" + token);
        verify(redisTemplate, times(1)).delete("verification:token:" + token);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 완료 실패 테스트 - 토큰 없음")
    void completeRegistrationFailureInvalidTokenTest() {
        // Given
        String token = "invalidToken";
        when(valueOperations.get("verification:token:" + token)).thenReturn(null);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
            userService.completeRegistration(token, "password123", "NewUser")
        );
        assertEquals(ErrorCode.INVALID_TOKEN, exception.getErrorCode());
        verify(valueOperations, times(1)).get("verification:token:" + token);
    }

    @Test
    @DisplayName("회원가입 완료 실패 테스트 - 토큰 만료")
    void completeRegistrationFailureTokenExpiredTest() throws Exception {
        // Given
        String token = "expiredToken";
        VerificationTokenDto tokenDto = new VerificationTokenDto(
            token, "expired@example.com", LocalDateTime.now().minusHours(1)
        );
        String tokenData = objectMapper.writeValueAsString(tokenDto);

        when(valueOperations.get("verification:token:" + token)).thenReturn(tokenData);
        when(objectMapper.readValue(tokenData, VerificationTokenDto.class)).thenReturn(tokenDto);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
            userService.completeRegistration(token, "password123", "ExpiredUser")
        );

        assertEquals(ErrorCode.TOKEN_EXPIRED, exception.getErrorCode());
        verify(valueOperations, times(1)).get("verification:token:" + token);
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

        when(userRepository.findByEmailAndDeletedAtIsNull("test@example.com"))
            .thenReturn(Optional.of(testUserWithId));

        when(passwordEncoder.matches("current_password", "encoded_password"))
            .thenReturn(true);

        when(passwordEncoder.encode("new_password123"))
            .thenReturn("encoded_new_password");

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
        UserUpdateResponse response = userService.updateUser("test@example.com", request);

        // Then
        assertEquals("test@example.com", response.email());
        assertEquals("new_nickname", response.nickname());
        assertEquals(Role.VISITOR, response.role());

        // Verify
        verify(userRepository, times(1)).findByEmailAndDeletedAtIsNull("test@example.com");
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

        User existingUser = User.builder()
            .email("test@example.com")
            .password("encoded_password")
            .nickname("old_nickname")
            .role(Role.VISITOR)
            .build();

        when(userRepository.findByEmailAndDeletedAtIsNull("test@example.com"))
            .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrong_password", "encoded_password")).thenReturn(false);

        // When & Then: 예외 검증
        CustomException exception = assertThrows(CustomException.class, () ->
            userService.updateUser("test@example.com", request)
        );

        assertEquals(ErrorCode.INVALID_PASSWORD, exception.getErrorCode());

        // Verify: Mock 메서드 호출 검증
        verify(userRepository, times(1)).findByEmailAndDeletedAtIsNull("test@example.com");
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

        when(userRepository.findByEmailAndDeletedAtIsNull("notfound@example.com"))
            .thenReturn(Optional.empty());

        // When & Then: 예외 검증
        CustomException exception = assertThrows(CustomException.class, () ->
            userService.updateUser("notfound@example.com", request)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());

        // Verify: Mock 메서드 호출 검증
        verify(userRepository, times(1)).findByEmailAndDeletedAtIsNull("notfound@example.com");
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    @DisplayName("회원 탈퇴 성공 테스트")
    void deleteUserSuccessTest() {
        // Given: 기존 사용자를 설정하고 Mock 동작 정의
        User existingUser = User.builder()
            .email("test@example.com")
            .password("encoded_password")
            .nickname("Tester")
            .role(Role.VISITOR)
            .build();

        when(userRepository.findByEmailAndDeletedAtIsNull("test@example.com"))
            .thenReturn(Optional.of(existingUser));

        // When: 회원 탈퇴 서비스 호출
        userService.deleteUser("test@example.com");

        // Then: 회원 탈퇴가 정상적으로 처리되었는지 검증
        verify(userRepository, times(1)).findByEmailAndDeletedAtIsNull("test@example.com");
        assertNotNull(existingUser.getDeletedAt(), "DeletedAt 필드가 null이 아니어야 합니다.");
    }

    @Test
    @DisplayName("회원 탈퇴 실패 테스트 - 사용자 없음")
    void deleteUserFailureUserNotFoundTest() {
        // Given
        when(userRepository.findByEmailAndDeletedAtIsNull("notfound@example.com"))
            .thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
            userService.deleteUser("notfound@example.com")
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository, times(1)).findByEmailAndDeletedAtIsNull("notfound@example.com");
    }

    @Test
    @DisplayName("회원 탈퇴 실패 테스트 - 이미 탈퇴된 사용자")
    void deleteUserFailureAlreadyDeletedTest() {
        // Given
        User deletedUser = User.builder()
            .email("deleted@example.com")
            .password("encoded_password")
            .nickname("DeletedUser")
            .role(Role.VISITOR)
            .build();

        deletedUser.delete();
        when(userRepository.findByEmailAndDeletedAtIsNull("deleted@example.com"))
            .thenReturn(Optional.of(deletedUser));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
            userService.deleteUser("deleted@example.com")
        );

        assertEquals(ErrorCode.USER_ALREADY_DELETED, exception.getErrorCode());
        verify(userRepository, times(1)).findByEmailAndDeletedAtIsNull("deleted@example.com");
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 성공 테스트")
    void requestPasswordResetSuccessTest() throws Exception {
        // Given
        PasswordResetRequestDto request = new PasswordResetRequestDto("test@example.com");
        when(userRepository.findByEmailAndDeletedAtIsNull(request.email()))
            .thenReturn(Optional.of(testUser));

        // When
        userService.requestPasswordReset(request);

        // Then
        verify(userRepository, times(1)).findByEmailAndDeletedAtIsNull(request.email());
        verify(valueOperations, times(1))
            .set(startsWith("password-reset:token:"), anyString(), eq(Duration.ofHours(24)));
        verify(mailUtil, times(1)).sendPasswordResetMail(eq(request.email()), anyString());
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 실패 테스트 - 사용자 없음")
    void requestPasswordResetFailureUserNotFoundTest() {
        // Given
        PasswordResetRequestDto request = new PasswordResetRequestDto("notfound@example.com");
        when(userRepository.findByEmailAndDeletedAtIsNull(request.email()))
            .thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
            userService.requestPasswordReset(request)
        );
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository, times(1)).findByEmailAndDeletedAtIsNull(request.email());
        verifyNoInteractions(mailUtil);
    }

    @Test
    @DisplayName("비밀번호 재설정 확인 성공 테스트")
    void confirmPasswordResetSuccessTest() throws Exception {
        // Given
        String token = "validToken";
        PasswordResetConfirmDto request = new PasswordResetConfirmDto(token, "new_password123");
        VerificationTokenDto tokenDto = new VerificationTokenDto(
            token, "reset@example.com", LocalDateTime.now().plusHours(1)
        );
        String tokenData = objectMapper.writeValueAsString(tokenDto);

        when(valueOperations.get("password-reset:token:" + token)).thenReturn(tokenData);
        when(objectMapper.readValue(tokenData, VerificationTokenDto.class)).thenReturn(tokenDto);
        when(userRepository.findByEmailAndDeletedAtIsNull(tokenDto.email()))
            .thenReturn(Optional.of(testUser));

        // When
        userService.confirmPasswordReset(request);

        // Then
        verify(valueOperations, times(1)).get("password-reset:token:" + token);
        verify(userRepository, times(1)).findByEmailAndDeletedAtIsNull(tokenDto.email());
        verify(passwordEncoder, times(1)).encode(request.newPassword());
        verify(redisTemplate, times(1)).delete("password-reset:token:" + token);
    }

    @Test
    @DisplayName("비밀번호 재설정 확인 실패 테스트 - 유효하지 않은 토큰")
    void confirmPasswordResetFailureInvalidTokenTest() {
        // Given
        String token = "invalidToken";
        PasswordResetConfirmDto request = new PasswordResetConfirmDto(token, "new_password123");
        when(valueOperations.get("password-reset:token:" + token)).thenReturn(null);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
            userService.confirmPasswordReset(request)
        );
        assertEquals(ErrorCode.INVALID_TOKEN, exception.getErrorCode());
        verify(valueOperations, times(1)).get("password-reset:token:" + token);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("비밀번호 재설정 확인 실패 테스트 - 토큰 만료")
    void confirmPasswordResetFailureTokenExpiredTest() throws Exception {
        // Given
        String token = "expiredToken";
        PasswordResetConfirmDto request = new PasswordResetConfirmDto(token, "new_password123");
        VerificationTokenDto tokenDto = new VerificationTokenDto(
            token, "reset@example.com", LocalDateTime.now().minusHours(1)
        );
        String tokenData = objectMapper.writeValueAsString(tokenDto);

        when(valueOperations.get("password-reset:token:" + token)).thenReturn(tokenData);
        when(objectMapper.readValue(tokenData, VerificationTokenDto.class)).thenReturn(tokenDto);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
            userService.confirmPasswordReset(request)
        );
        assertEquals(ErrorCode.TOKEN_EXPIRED, exception.getErrorCode());
        verify(valueOperations, times(1)).get("password-reset:token:" + token);
    }
}