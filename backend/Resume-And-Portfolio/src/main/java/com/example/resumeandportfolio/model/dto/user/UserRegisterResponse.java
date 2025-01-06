package com.example.resumeandportfolio.model.dto.user;

import com.example.resumeandportfolio.model.enums.Role;

/**
 * 회원 가입 응답 DTO
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

public record UserRegisterResponse(
    Long userId,
    String email,
    String nickname,
    Role role
) {}