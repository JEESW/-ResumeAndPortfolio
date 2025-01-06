package com.example.resumeandportfolio.util.mapper;

import com.example.resumeandportfolio.model.dto.user.UserLoginResponse;
import com.example.resumeandportfolio.model.entity.user.User;

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
}