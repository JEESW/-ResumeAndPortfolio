package com.example.resumeandportfolio.controller.user;

import com.example.resumeandportfolio.exception.CustomException;
import com.example.resumeandportfolio.exception.ErrorCode;
import com.example.resumeandportfolio.exception.GlobalExceptionHandler;
import com.example.resumeandportfolio.model.dto.user.*;
import com.example.resumeandportfolio.model.enums.Role;
import com.example.resumeandportfolio.service.global.RefreshTokenService;
import com.example.resumeandportfolio.service.user.UserService;
import com.example.resumeandportfolio.util.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * User's Controller Test
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    private void mockSecurityContext(String email) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            email, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccessTest() throws Exception {
        // Given
        UserLoginRequest request = new UserLoginRequest("test@example.com", "password");
        UserLoginResponse response = new UserLoginResponse(1L, "test@example.com", "tester",
            Role.valueOf("VISITOR"));

        when(userService.login(request.email(), request.password())).thenReturn(response);
        when(jwtUtil.createJwt(anyString(), anyString(), anyString(), anyLong())).thenReturn(
            "dummyAccessToken", "dummyRefreshToken");

        // When & Then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(userService, times(1)).login(request.email(), request.password());
        verify(refreshTokenService, times(1)).saveRefreshToken(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 사용자 없음")
    void loginFailureUserNotFoundTest() throws Exception {
        // Given
        UserLoginRequest request = new UserLoginRequest("notfound@example.com", "password");

        when(userService.login(request.email(), request.password()))
            .thenThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

        // When & Then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 잘못된 비밀번호")
    void loginFailureInvalidPasswordTest() throws Exception {
        // Given
        UserLoginRequest request = new UserLoginRequest("test@example.com", "wrong_password");

        // Mocking: UserService.login 호출 시 CustomException 발생
        when(userService.login(request.email(), request.password()))
            .thenThrow(new CustomException(ErrorCode.INVALID_PASSWORD));

        // When & Then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void registerSuccessTest() throws Exception {
        // Given
        UserRegisterRequest request = new UserRegisterRequest("test@example.com", "password123",
            "tester");
        UserRegisterResponse response = new UserRegisterResponse(1L, "test@example.com", "tester",
            Role.valueOf("VISITOR"));

        when(userService.register(any(UserRegisterRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 이메일 중복")
    void registerFailureEmailAlreadyExistsTest() throws Exception {
        // Given
        UserRegisterRequest request = new UserRegisterRequest(
            "duplicate@example.com",
            "password123",
            "nickname"
        );

        // Mocking
        when(userService.register(any(UserRegisterRequest.class)))
            .thenThrow(new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS));

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 유효하지 않은 요청 데이터")
    void registerFailureInvalidRequestTest() throws Exception {
        // Given: 잘못된 요청 데이터 (비밀번호 길이 부족)
        UserRegisterRequest request = new UserRegisterRequest(
            "invalid@example.com",
            "short",
            "nickname"
        );

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원 수정 성공 테스트")
    void updateUserSuccessTest() throws Exception {
        // Given
        UserUpdateRequest request = new UserUpdateRequest("new_nickname", "current_password", "new_password123");
        UserUpdateResponse response = new UserUpdateResponse(1L, "test@example.com", "new_nickname", Role.VISITOR);

        mockSecurityContext("test@example.com");

        when(userService.updateUser(anyString(), any(UserUpdateRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/users/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원 수정 실패 테스트 - 사용자 없음")
    void updateUserFailureUserNotFoundTest() throws Exception {
        // Given
        UserUpdateRequest request = new UserUpdateRequest("new_nickname", "current_password", "new_password123");

        // SecurityContext 설정
        mockSecurityContext("test@example.com");

        when(userService.updateUser(anyString(), any(UserUpdateRequest.class)))
            .thenThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

        // When & Then
        mockMvc.perform(put("/api/users/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("회원 수정 실패 테스트 - 비밀번호 불일치")
    void updateUserFailureInvalidPasswordTest() throws Exception {
        // Given
        UserUpdateRequest request = new UserUpdateRequest("new_nickname", "wrong_password", "new_password123");

        // SecurityContext 설정
        mockSecurityContext("test@example.com");

        when(userService.updateUser(anyString(), any(UserUpdateRequest.class)))
            .thenThrow(new CustomException(ErrorCode.INVALID_PASSWORD));

        // When & Then
        mockMvc.perform(put("/api/users/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("회원 수정 실패 테스트 - 닉네임과 비밀번호가 모두 null")
    void updateUserFailureInvalidRequestTest() throws Exception {
        // Given
        UserUpdateRequest request = new UserUpdateRequest(null, "current_password", null);

        // SecurityContext 설정
        mockSecurityContext("test@example.com");

        when(userService.updateUser(anyString(), any(UserUpdateRequest.class)))
            .thenThrow(new CustomException(ErrorCode.INVALID_REQUEST));

        // When & Then
        mockMvc.perform(put("/api/users/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원 탈퇴 성공 테스트")
    void deleteUserSuccessTest() throws Exception {
        // Given
        mockSecurityContext("test@example.com");

        // When & Then
        mockMvc.perform(delete("/api/users/delete"))
            .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser("test@example.com");
        verify(refreshTokenService, times(1)).deleteRefreshToken("test@example.com");
    }

    @Test
    @DisplayName("회원 탈퇴 실패 테스트 - 로그인되지 않은 사용자")
    void deleteUserFailureUnauthorizedTest() throws Exception {
        // Given: SecurityContext에 인증 정보가 없는 상태로 설정
        SecurityContextHolder.clearContext();

        // When & Then
        mockMvc.perform(delete("/api/users/delete")
                .header("Authorization", ""))
            .andExpect(status().isUnauthorized());

        // Verify: 서비스 메서드 호출이 없어야 함
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("회원 탈퇴 실패 테스트 - 사용자 없음")
    void deleteUserFailureUserNotFoundTest() throws Exception {
        // Given
        mockSecurityContext("test@example.com");
        doThrow(new CustomException(ErrorCode.USER_NOT_FOUND)).when(userService).deleteUser(anyString());

        // When & Then
        mockMvc.perform(delete("/api/users/delete"))
            .andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUser(anyString());
    }

    @Test
    @DisplayName("회원 탈퇴 실패 테스트 - 이미 탈퇴된 사용자")
    void deleteUserFailureAlreadyDeletedTest() throws Exception {
        // Given
        mockSecurityContext("test@example.com");
        doThrow(new CustomException(ErrorCode.USER_ALREADY_DELETED)).when(userService).deleteUser(anyString());

        // When & Then
        mockMvc.perform(delete("/api/users/delete"))
            .andExpect(status().isBadRequest());

        verify(userService, times(1)).deleteUser(anyString());
    }
}