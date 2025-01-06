package com.example.resumeandportfolio.controller.user;

import com.example.resumeandportfolio.model.dto.user.UserLoginRequest;
import com.example.resumeandportfolio.model.dto.user.UserLoginResponse;
import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.service.user.UserService;
import com.example.resumeandportfolio.util.mapper.UserMapper;
import jakarta.validation.Valid;
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
    public ResponseEntity<UserLoginResponse> login(
        @Valid @RequestBody UserLoginRequest request,
        HttpSession session
    ) {
        User user = userService.login(request.email(), request.password());

        session.setAttribute("user", user);

        UserLoginResponse response = UserMapper.toLoginResponse(user);

        return ResponseEntity.ok(response);
    }

    // 로그아웃 API
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate(); // 세션 무효화
        return ResponseEntity.ok("로그아웃 성공");
    }
}