package com.example.resumeandportfolio.controller.user;

import com.example.resumeandportfolio.exception.CustomException;
import com.example.resumeandportfolio.exception.ErrorCode;
import com.example.resumeandportfolio.model.dto.user.UserLoginRequest;
import com.example.resumeandportfolio.model.dto.user.UserLoginResponse;
import com.example.resumeandportfolio.model.dto.user.UserRegisterResponse;
import com.example.resumeandportfolio.model.dto.user.UserRegisterRequest;
import com.example.resumeandportfolio.model.dto.user.UserUpdateRequest;
import com.example.resumeandportfolio.model.dto.user.UserUpdateResponse;
import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.service.user.UserService;
import com.example.resumeandportfolio.util.mapper.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    // 회원 가입 API
    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponse> register(
        @Valid @RequestBody UserRegisterRequest request) {
        UserRegisterResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 회원 수정 API
    @PutMapping("/update")
    public ResponseEntity<UserUpdateResponse> updateUser(
        @Valid @RequestBody UserUpdateRequest request,
        HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");

        // 로그인되지 않은 사용자인 경우 401 Unauthorized 반환
        if (sessionUser == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        User updatedUser = userService.updateUser(sessionUser.getUserId(), request);

        session.setAttribute("user", updatedUser);

        UserUpdateResponse response = UserMapper.toUpdateResponse(updatedUser);

        return ResponseEntity.ok(response);
    }
}