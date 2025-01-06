package com.example.resumeandportfolio.controller.user;

import com.example.resumeandportfolio.exception.CustomException;
import com.example.resumeandportfolio.exception.ErrorCode;
import com.example.resumeandportfolio.exception.GlobalExceptionHandler;
import com.example.resumeandportfolio.model.dto.user.UserLoginResponse;
import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.model.enums.Role;
import com.example.resumeandportfolio.service.user.UserService;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccessTest() throws Exception {
        // Given: 로그인 성공 시 반환할 사용자 설정
        User user = new User("test@example.com", "encoded_password", "tester", Role.VISITOR);

        UserLoginResponse response = new UserLoginResponse(
            user.getUserId(),
            user.getEmail(),
            user.getNickname()
        );

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
}