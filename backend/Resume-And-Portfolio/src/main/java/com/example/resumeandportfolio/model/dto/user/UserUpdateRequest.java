package com.example.resumeandportfolio.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원 수정 요청 DTO
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

public record UserUpdateRequest(
    @Size(max = 15, message = "닉네임은 최대 15자까지 가능합니다.")
    String nickname,

    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    String currentPassword,

    @Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다.")
    String newPassword
) {}