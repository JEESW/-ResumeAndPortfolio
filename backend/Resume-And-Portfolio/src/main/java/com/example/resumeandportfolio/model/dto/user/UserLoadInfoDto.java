package com.example.resumeandportfolio.model.dto.user;

/**
 * 사용자 정보 불러오기 DTO
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

public record UserLoadInfoDto(
    String email,
    String nickname,
    String role
) {}