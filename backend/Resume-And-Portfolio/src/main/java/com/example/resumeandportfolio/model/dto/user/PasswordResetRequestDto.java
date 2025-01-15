package com.example.resumeandportfolio.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Password Reset Request DTO
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

public record PasswordResetRequestDto(
    @Email(message = "올바른 이메일 형식을 입력하세요.")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    String email
) {}