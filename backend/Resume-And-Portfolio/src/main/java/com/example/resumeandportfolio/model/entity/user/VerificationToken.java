package com.example.resumeandportfolio.model.entity.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Email Verification Token Entity
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@Entity
@Table(name = "verification_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerificationToken {

    // ID(PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 인증 토큰
    @Column(nullable = false, unique = true)
    private String token;

    // 이메일
    @Column(nullable = false)
    private String email;

    // 유효 기간
    @Column(nullable = false)
    private LocalDateTime expiration;

    @Builder
    public VerificationToken(String token, String email, LocalDateTime expiration) {
        this.token = token;
        this.email = email;
        this.expiration = expiration;
    }
}