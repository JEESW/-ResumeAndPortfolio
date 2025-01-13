package com.example.resumeandportfolio.model.dto.user;

import com.example.resumeandportfolio.model.enums.Role;

/**
 * 로그인 응답 DTO
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

public record UserLoginResponse(
    Long userId,
    String email,
    String nickname,
    Role role
) {}