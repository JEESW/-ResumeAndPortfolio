package com.example.resumeandportfolio.model.enums;

import lombok.Getter;

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
}