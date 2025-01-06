package com.example.resumeandportfolio.controller.user;

import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

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

    // 로그인 API
    @PostMapping("/login")
    public ResponseEntity<String> login(
        @RequestParam String email,
        @RequestParam String password,
        HttpSession session
    ) {
        User user = userService.login(email, password);

        // 세션에 사용자 정보 저장
        session.setAttribute("user", user);

        return ResponseEntity.ok("로그인 성공");
    }

    // 로그아웃 API
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate(); // 세션 무효화
        return ResponseEntity.ok("로그아웃 성공");
    }
}