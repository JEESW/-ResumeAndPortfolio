package com.example.resumeandportfolio.util.mapper;

import com.example.resumeandportfolio.model.dto.user.UserDto;
import com.example.resumeandportfolio.model.entity.user.User;

/**
 * User's DTO Mapper
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

public class UserDtoMapper {

    // Entity → DTO
    public static UserDto toDto(User user) {
        return new UserDto(
            user.getUserId(),
            user.getEmail(),
            user.getNickname(),
            user.getRole()
        );
    }

    // DTO → Entity
    public static User toEntity(UserDto userDto, String encodedPassword) {
        return User.builder()
            .email(userDto.email())
            .password(encodedPassword)
            .nickname(userDto.nickname())
            .role(userDto.role())
            .build();
    }
}