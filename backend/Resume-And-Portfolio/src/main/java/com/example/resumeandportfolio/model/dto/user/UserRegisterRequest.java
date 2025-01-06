package com.example.resumeandportfolio.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원 가입 요청 DTO
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

public record UserRegisterRequest(
    @Email(message = "올바른 이메일 형식을 입력하세요.")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    String email,

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다.")
    String password,

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(max = 15, message = "닉네임은 최대 15자까지 가능합니다.")
    String nickname
) {}