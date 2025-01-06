package com.example.resumeandportfolio.model.entity.user;

import com.example.resumeandportfolio.model.entity.global.BaseEntity;
import com.example.resumeandportfolio.model.enums.Role;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * User Entity
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    // User ID(PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    // 이메일
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    // 패스워드
    @Column(nullable = false, length = 255)
    private String password;

    // 닉네임
    @Column(nullable = false, length = 15)
    private String nickname;

    // 역할
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // 삭제 일자
    @Column
    private LocalDateTime deletedAt;

    @Builder
    public User(String email, String password, String nickname, Role role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }

    // 소프트 딜리트 메서드
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}