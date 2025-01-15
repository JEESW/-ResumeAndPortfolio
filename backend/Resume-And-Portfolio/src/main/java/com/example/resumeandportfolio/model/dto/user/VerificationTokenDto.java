package com.example.resumeandportfolio.model.dto.user;

import java.time.LocalDateTime;

/**
 * Email Verification Token DTO
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

public record VerificationTokenDto(
    String token,
    String email,
    LocalDateTime expiration
) {
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiration);
    }
}