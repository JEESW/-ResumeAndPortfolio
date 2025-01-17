package com.example.resumeandportfolio.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Password Reset Confirm DTO
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

public record PasswordResetConfirmDto(
    @NotBlank(message = "토큰은 필수 입력 항목입니다.")
    String token,

    @NotBlank(message = "새 비밀번호는 필수 입력 항목입니다.")
    @Size(min = 6, message = "새 비밀번호는 최소 6자 이상이어야 합니다.")
    String newPassword
) {}