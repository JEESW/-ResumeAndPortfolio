package com.example.resumeandportfolio.model.dto.user;

import com.example.resumeandportfolio.model.enums.Role;

/**
 * 회원 수정 응답 DTO
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

public record UserUpdateResponse(
    Long userId,
    String email,
    String nickname,
    Role role
) {}