package com.example.resumeandportfolio.model.enums;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Role Enum
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@Getter
public enum Role {
    ADMIN("관리자"),
    VISITOR("방문자");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    // 권한 리스트 반환
    public List<SimpleGrantedAuthority> getGrantedAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.name()));
    }
}