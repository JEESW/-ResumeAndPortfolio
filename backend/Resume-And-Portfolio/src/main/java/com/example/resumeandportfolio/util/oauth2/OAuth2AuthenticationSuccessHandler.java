package com.example.resumeandportfolio.util.oauth2;

import com.example.resumeandportfolio.service.user.RefreshTokenService;
import com.example.resumeandportfolio.util.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * OAuth2 Authentication Success Handler
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        // JWT 생성
        String accessToken = jwtUtil.createJwt("access", email, "ROLE_USER", 600000L);
        String refreshToken = jwtUtil.createJwt("refresh", email, "ROLE_USER", 86400000L);

        // Redis에 Refresh Token 저장
        refreshTokenService.saveRefreshToken(email, refreshToken, 86400L);

        // 쿠키로 Access Token과 Refresh Token 전달
        response.addCookie(createCookie("access", accessToken));
        response.addCookie(createCookie("refresh", refreshToken));

        response.sendRedirect("https://www.jsw-resumeandportfolio.com/social-login/success");
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);

        return cookie;
    }
}