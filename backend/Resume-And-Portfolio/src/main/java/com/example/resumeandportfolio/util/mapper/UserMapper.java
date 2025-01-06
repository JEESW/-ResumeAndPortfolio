package com.example.resumeandportfolio.util.mapper;

import com.example.resumeandportfolio.model.dto.user.UserLoginResponse;
import com.example.resumeandportfolio.model.dto.user.UserRegisterRequest;
import com.example.resumeandportfolio.model.dto.user.UserRegisterResponse;
import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.model.enums.Role;

/**
 * User's Mapper
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

public class UserMapper {

    // Entity → UserLoginResponse DTO
    public static UserLoginResponse toLoginResponse(User user) {
        return new UserLoginResponse(
            user.getUserId(),
            user.getEmail(),
            user.getNickname()
        );
    }

    // Entity → UserRegisterResponse DTO
    public static UserRegisterResponse toResponse(User user) {
        return new UserRegisterResponse(
            user.getUserId(),
            user.getEmail(),
            user.getNickname(),
            user.getRole()
        );
    }

    // UserRegisterRequest DTO → Entity
    public static User toEntity(UserRegisterRequest request, String encodedPassword) {
        return User.builder()
            .email(request.email())
            .password(encodedPassword)
            .nickname(request.nickname())
            .role(Role.VISITOR)
            .build();
    }
}