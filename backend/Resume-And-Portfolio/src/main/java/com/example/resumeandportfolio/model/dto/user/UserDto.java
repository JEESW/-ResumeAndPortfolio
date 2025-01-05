package com.example.resumeandportfolio.model.dto.user;

import com.example.resumeandportfolio.model.enums.Role;

/**
 * User DTO
 *
 * Request: 회원가입/수정 시 사용
 * Response: API 응답 시 사용
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

public record UserDto(
    Long userId,
    String email,
    String nickname,
    Role role
) {
    public UserDto(String email, String nickname, Role role) {
        this(null, email, nickname, role);
    }
}