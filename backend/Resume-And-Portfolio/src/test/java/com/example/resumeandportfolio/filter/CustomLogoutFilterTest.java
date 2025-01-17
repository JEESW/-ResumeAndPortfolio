package com.example.resumeandportfolio.filter;

import com.example.resumeandportfolio.service.user.RefreshTokenService;
import com.example.resumeandportfolio.util.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Custom Logout Filter Test
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

class CustomLogoutFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private CustomLogoutFilter customLogoutFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("로그아웃 성공 테스트 - Refresh Token이 유효하고 삭제된 경우")
    void logoutSuccessTest() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users/logout");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        String refreshToken = "validRefreshToken";
        String username = "user@example.com";

        request.setCookies(new Cookie("refresh", refreshToken));

        when(jwtUtil.isExpired(refreshToken)).thenReturn(false);
        when(jwtUtil.getCategory(refreshToken)).thenReturn("refresh");
        when(jwtUtil.getUsername(refreshToken)).thenReturn(username);
        when(refreshTokenService.getRefreshToken(username)).thenReturn(refreshToken);

        // When
        customLogoutFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(response.getCookies()).anyMatch(cookie ->
            "refresh".equals(cookie.getName()) && cookie.getMaxAge() == 0
        );
        verify(refreshTokenService, times(1)).deleteRefreshToken(username);
    }

    @Test
    @DisplayName("로그아웃 실패 테스트 - Refresh Token 없음")
    void logoutFailureNoRefreshTokenTest() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users/logout");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // When
        customLogoutFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
        verifyNoInteractions(jwtUtil, refreshTokenService);
    }

    @Test
    @DisplayName("로그아웃 실패 테스트 - Refresh Token 만료됨")
    void logoutFailureExpiredRefreshTokenTest() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users/logout");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        String refreshToken = "expiredRefreshToken";
        request.setCookies(new Cookie("refresh", refreshToken));

        doThrow(ExpiredJwtException.class).when(jwtUtil).isExpired(refreshToken);

        // When
        customLogoutFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
        verify(jwtUtil, times(1)).isExpired(refreshToken);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    @DisplayName("로그아웃 실패 테스트 - 잘못된 토큰 카테고리")
    void logoutFailureInvalidCategoryTest() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users/logout");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        String refreshToken = "invalidCategoryToken";
        request.setCookies(new Cookie("refresh", refreshToken));

        when(jwtUtil.isExpired(refreshToken)).thenReturn(false);
        when(jwtUtil.getCategory(refreshToken)).thenReturn("access");

        // When
        customLogoutFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
        verify(jwtUtil, times(1)).getCategory(refreshToken);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    @DisplayName("로그아웃 실패 테스트 - Redis에 저장된 토큰과 불일치")
    void logoutFailureTokenMismatchTest() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users/logout");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        String refreshToken = "validRefreshToken";
        String username = "user@example.com";

        request.setCookies(new Cookie("refresh", refreshToken));

        when(jwtUtil.isExpired(refreshToken)).thenReturn(false);
        when(jwtUtil.getCategory(refreshToken)).thenReturn("refresh");
        when(jwtUtil.getUsername(refreshToken)).thenReturn(username);
        when(refreshTokenService.getRefreshToken(username)).thenReturn("differentToken");

        // When
        customLogoutFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
        verify(refreshTokenService, times(1)).getRefreshToken(username);
        verify(refreshTokenService, times(0)).deleteRefreshToken(username);
    }
}