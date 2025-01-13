package com.example.resumeandportfolio.controller.user;

import com.example.resumeandportfolio.exception.CustomException;
import com.example.resumeandportfolio.exception.ErrorCode;
import com.example.resumeandportfolio.model.dto.user.UserLoginRequest;
import com.example.resumeandportfolio.model.dto.user.UserLoginResponse;
import com.example.resumeandportfolio.model.dto.user.UserRegisterResponse;
import com.example.resumeandportfolio.model.dto.user.UserRegisterRequest;
import com.example.resumeandportfolio.model.dto.user.UserUpdateRequest;
import com.example.resumeandportfolio.model.dto.user.UserUpdateResponse;
import com.example.resumeandportfolio.service.global.RefreshTokenService;
import com.example.resumeandportfolio.service.user.UserService;
import com.example.resumeandportfolio.util.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * User's Controller
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    // 로그인 API
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(
        @Valid @RequestBody UserLoginRequest request,
        HttpServletResponse response
    ) {
        UserLoginResponse loginResponse = userService.login(request.email(), request.password());

        String accessToken = jwtUtil.createJwt("access", loginResponse.email(),
            loginResponse.role().name(), 600000L);
        String refreshToken = jwtUtil.createJwt("refresh", loginResponse.email(),
            loginResponse.role().name(), 86400000L);

        // Redis에 Refresh 토큰 저장
        refreshTokenService.saveRefreshToken(loginResponse.email(), refreshToken, 86400L);

        response.setHeader("Authorization", "Bearer " + accessToken);
        response.addCookie(createCookie("refresh", refreshToken));

        return ResponseEntity.ok(loginResponse);
    }

    // 회원 가입 API
    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponse> register(
        @Valid @RequestBody UserRegisterRequest request
    ) {
        UserRegisterResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 회원 수정 API
    @PutMapping("/update")
    public ResponseEntity<UserUpdateResponse> updateUser(
        @Valid @RequestBody UserUpdateRequest request,
        HttpServletRequest httpServletRequest
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        UserUpdateResponse response = userService.updateUser(email, request);
        return ResponseEntity.ok(response);
    }

    // 회원 탈퇴 API
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 없는 경우 처리
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        String email = authentication.getName();

        userService.deleteUser(email);
        refreshTokenService.deleteRefreshToken(email); // Redis에서 Refresh 토큰 삭제

        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }

    // 쿠키 생성 메서드
    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setHttpOnly(true);

        return cookie;
    }
}