package com.example.resumeandportfolio.service.user;

import com.example.resumeandportfolio.exception.CustomException;
import com.example.resumeandportfolio.exception.ErrorCode;
import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * User's Service
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 로그인 로직
    public User login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        return user;
    }
}