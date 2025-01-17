package com.example.resumeandportfolio.util.oauth2;

import com.example.resumeandportfolio.service.user.RefreshTokenService;
import com.example.resumeandportfolio.util.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * OAuth2 Authentication Success Handler Test
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    private OAuth2AuthenticationSuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        successHandler = new OAuth2AuthenticationSuccessHandler(jwtUtil, refreshTokenService);
    }

    @Test
    @DisplayName("기존 사용자를 로드할 때 성공")
    void loadUser_WhenUserExists() throws IOException {
        // Given
        String email = "test@example.com";
        String role = "ROLE_USER";

        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            Collections.emptySet(),
            Collections.singletonMap("email", email),
            "email"
        );

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            mockOAuth2User,
            null,
            mockOAuth2User.getAuthorities()
        );

        when(jwtUtil.createJwt("access", email, role, 600000L)).thenReturn("mock-access-token");
        when(jwtUtil.createJwt("refresh", email, role, 86400000L)).thenReturn("mock-refresh-token");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(refreshTokenService, times(1)).saveRefreshToken(email, "mock-refresh-token", 86400L);
        verify(response, times(1)).addCookie(argThat(
            cookie -> "access".equals(cookie.getName()) && "mock-access-token".equals(
                cookie.getValue())));
        verify(response, times(1)).addCookie(argThat(
            cookie -> "refresh".equals(cookie.getName()) && "mock-refresh-token".equals(
                cookie.getValue())));
        verify(response, times(1)).sendRedirect(
            "https://jsw-resumeandportfolio.com/social-login/success");
    }

    @Test
    @DisplayName("새로운 사용자를 저장할 때 성공")
    void loadUser_WhenUserDoesNotExist() throws IOException {
        // Given
        String email = "newuser@example.com";
        String role = "ROLE_USER";

        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            Collections.emptySet(),
            Collections.singletonMap("email", email),
            "email"
        );

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            mockOAuth2User,
            null,
            mockOAuth2User.getAuthorities()
        );

        when(jwtUtil.createJwt("access", email, role, 600000L)).thenReturn("mock-access-token");
        when(jwtUtil.createJwt("refresh", email, role, 86400000L)).thenReturn("mock-refresh-token");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(refreshTokenService, times(1)).saveRefreshToken(email, "mock-refresh-token", 86400L);
        verify(response, times(1)).addCookie(argThat(
            cookie -> "access".equals(cookie.getName()) && "mock-access-token".equals(
                cookie.getValue())));
        verify(response, times(1)).addCookie(argThat(
            cookie -> "refresh".equals(cookie.getName()) && "mock-refresh-token".equals(
                cookie.getValue())));
        verify(response, times(1)).sendRedirect(
            "https://jsw-resumeandportfolio.com/social-login/success");
    }
}