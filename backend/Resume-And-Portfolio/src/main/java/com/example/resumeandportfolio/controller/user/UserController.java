package com.example.resumeandportfolio.controller.user;

import com.example.resumeandportfolio.exception.CustomException;
import com.example.resumeandportfolio.exception.ErrorCode;
import com.example.resumeandportfolio.model.dto.user.PasswordResetConfirmDto;
import com.example.resumeandportfolio.model.dto.user.PasswordResetRequestDto;
import com.example.resumeandportfolio.model.dto.user.UserLoginRequest;
import com.example.resumeandportfolio.model.dto.user.UserLoginResponse;
import com.example.resumeandportfolio.model.dto.user.UserRegisterResponse;
import com.example.resumeandportfolio.model.dto.user.UserRegisterRequest;
import com.example.resumeandportfolio.model.dto.user.UserUpdateRequest;
import com.example.resumeandportfolio.model.dto.user.UserUpdateResponse;
import com.example.resumeandportfolio.service.user.RefreshTokenService;
import com.example.resumeandportfolio.service.user.UserService;
import com.example.resumeandportfolio.util.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    // 회원 가입 이메일 인증 요청 API
    @PostMapping("/register/initiate")
    public ResponseEntity<String> initiateRegistration(
        @Valid @RequestBody UserRegisterRequest request) {
        userService.initiateRegistration(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body("인증 이메일이 발송되었습니다.");
    }

    // 인증 이메일 재전송 API
    @PostMapping("/register/resend")
    public ResponseEntity<String> resendVerificationEmail(@RequestParam String email) {
        userService.resendVerificationEmail(email);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body("새로운 인증 이메일이 발송되었습니다.");
    }

    // 회원 가입 완료 API
    @PostMapping("/register/complete")
    public ResponseEntity<UserRegisterResponse> completeRegistration(
        @RequestParam String token,
        @RequestParam String password,
        @RequestParam String nickname
    ) {
        UserRegisterResponse response = userService.completeRegistration(token, password, nickname);
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

    // 비밀번호 재설정 요청 API
    @PostMapping("/reset-password/request")
    public ResponseEntity<String> requestPasswordReset(
        @Valid @RequestBody PasswordResetRequestDto request
    ) {
        userService.requestPasswordReset(request);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body("비밀번호 재설정 이메일이 발송되었습니다.");
    }

    // 비밀번호 재설정 확인 API
    @PostMapping("/reset-password/confirm")
    public ResponseEntity<String> confirmPasswordReset(
        @Valid @RequestBody PasswordResetConfirmDto request
    ) {
        userService.confirmPasswordReset(request);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body("비밀번호가 성공적으로 변경되었습니다.");
    }

    // 쿠키 생성 메서드
    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setHttpOnly(true);

        return cookie;
    }
}