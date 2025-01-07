package com.example.resumeandportfolio.controller.user;

import com.example.resumeandportfolio.exception.CustomException;
import com.example.resumeandportfolio.exception.ErrorCode;
import com.example.resumeandportfolio.exception.GlobalExceptionHandler;
import com.example.resumeandportfolio.model.dto.user.UserRegisterRequest;
import com.example.resumeandportfolio.model.dto.user.UserRegisterResponse;
import com.example.resumeandportfolio.model.dto.user.UserUpdateRequest;
import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.model.enums.Role;
import com.example.resumeandportfolio.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccessTest() throws Exception {
        // Given: 로그인 성공 시 반환할 사용자 설정
        User user = new User("test@example.com", "encoded_password", "tester", Role.VISITOR);

        // Mocking: UserService.login 호출 시 성공적으로 사용자 반환
        Mockito.when(userService.login("test@example.com", "correct_password"))
            .thenReturn(user);

        // When & Then: API 호출 및 검증
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"test@example.com\", \"password\": \"correct_password\"}"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 존재하지 않는 사용자")
    void loginFailureUserNotFoundTest() throws Exception {
        // Mocking: UserService.login 호출 시 CustomException 발생
        Mockito.when(userService.login("notfound@example.com", "password"))
            .thenThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

        // When & Then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"notfound@example.com\", \"password\": \"password\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 잘못된 비밀번호")
    void loginFailureInvalidPasswordTest() throws Exception {
        // Mocking: UserService.login 호출 시 CustomException 발생
        Mockito.when(userService.login("test@example.com", "wrong_password"))
            .thenThrow(new CustomException(ErrorCode.INVALID_PASSWORD));

        // When & Then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"test@example.com\", \"password\": \"wrong_password\"}"))
            .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("로그아웃 성공 테스트")
    void logoutSuccessTest() throws Exception {
        // When & Then: API 호출 및 검증
        mockMvc.perform(post("/api/users/logout")
                .with(csrf()))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void registerSuccessTest() throws Exception {
        // Given
        UserRegisterRequest request = new UserRegisterRequest(
            "test@example.com",
            "password123",
            "nickname"
        );

        UserRegisterResponse response = new UserRegisterResponse(
            1L,
            "test@example.com",
            "nickname",
            null
        );

        // Mocking
        Mockito.when(userService.register(any(UserRegisterRequest.class)))
            .thenReturn(response);

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
        Mockito.when(userService.register(any(UserRegisterRequest.class)))
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
        UserUpdateRequest request = new UserUpdateRequest(
            "new_nickname",
            "current_password",
            "new_password123"
        );

        User user = new User(
            "test@example.com",
            "encoded_new_password",
            "new_nickname",
            Role.VISITOR
        );

        // Mocking
        Mockito.when(userService.updateUser(eq(1L), any(UserUpdateRequest.class)))
            .thenReturn(user);

        // When & Then
        mockMvc.perform(put("/api/users/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .sessionAttr("user", new User(
                    "test@example.com",
                    "encoded_password",
                    "old_nickname",
                    Role.VISITOR
                ) {
                    @Override
                    public Long getUserId() {
                        return 1L;
                    }
                }))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원 수정 실패 테스트 - 로그인되지 않은 사용자")
    void updateUserFailureUnauthorizedTest() throws Exception {
        // Given
        UserUpdateRequest request = new UserUpdateRequest(
            "new_nickname",
            "current_password",
            "new_password123"
        );

        // When & Then
        mockMvc.perform(put("/api/users/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("회원 수정 실패 테스트 - 비밀번호 불일치")
    void updateUserFailureInvalidPasswordTest() throws Exception {
        // Given
        UserUpdateRequest request = new UserUpdateRequest(
            "new_nickname",
            "wrong_password",
            "new_password123"
        );

        // Mocking
        Mockito.when(userService.updateUser(any(Long.class), any(UserUpdateRequest.class)))
            .thenThrow(new CustomException(ErrorCode.INVALID_PASSWORD));

        // When & Then
        mockMvc.perform(put("/api/users/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .sessionAttr("user", new User(
                    "test@example.com",
                    "encoded_password",
                    "old_nickname",
                    Role.VISITOR
                ) {
                    @Override
                    public Long getUserId() {
                        return 1L;
                    }
                }))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("회원 수정 실패 테스트 - 닉네임과 비밀번호가 모두 null")
    void updateUserFailureInvalidRequestTest() throws Exception {
        // Given
        UserUpdateRequest request = new UserUpdateRequest(
            null,
            "current_password",
            null
        );

        // Mocking
        Mockito.when(userService.updateUser(any(Long.class), any(UserUpdateRequest.class)))
            .thenThrow(new CustomException(ErrorCode.INVALID_REQUEST));

        // When & Then
        mockMvc.perform(put("/api/users/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .sessionAttr("user", new User(
                    "test@example.com",
                    "encoded_password",
                    "old_nickname",
                    Role.VISITOR
                ) {
                    @Override
                    public Long getUserId() {
                        return 1L;
                    }
                }))
            .andExpect(status().isBadRequest());
    }
}