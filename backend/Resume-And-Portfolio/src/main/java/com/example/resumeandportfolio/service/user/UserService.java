package com.example.resumeandportfolio.service.user;

import com.example.resumeandportfolio.exception.CustomException;
import com.example.resumeandportfolio.exception.ErrorCode;
import com.example.resumeandportfolio.model.dto.user.UserLoginResponse;
import com.example.resumeandportfolio.model.dto.user.UserRegisterRequest;
import com.example.resumeandportfolio.model.dto.user.UserRegisterResponse;
import com.example.resumeandportfolio.model.dto.user.UserUpdateRequest;
import com.example.resumeandportfolio.model.dto.user.UserUpdateResponse;
import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.repository.user.UserRepository;
import com.example.resumeandportfolio.util.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User's Service
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 로그인 로직
    @Transactional
    public UserLoginResponse login(String email, String rawPassword) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        return UserMapper.toLoginResponse(user);
    }

    // 회원 가입 로직
    @Transactional
    public UserRegisterResponse register(UserRegisterRequest request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = UserMapper.toEntity(request, encodedPassword);
        User savedUser = userRepository.save(user);

        return UserMapper.toResponse(savedUser);
    }

    // 회원 수정 로직
    @Transactional
    public UserUpdateResponse updateUser(String email, UserUpdateRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 닉네임과 새 비밀번호가 모두 null인 경우 예외 처리
        if (request.nickname() == null && request.newPassword() == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 현재 비밀번호 검증
        if (request.currentPassword() != null && !passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 닉네임 업데이트
        if (request.nickname() != null) {
            user.updateNickname(request.nickname());
        }

        // 새 비밀번호 업데이트
        if (request.newPassword() != null) {
            String encodedPassword = passwordEncoder.encode(request.newPassword());
            user.updatePassword(encodedPassword);
        }

        User updatedUser = userRepository.save(user);

        return UserMapper.toUpdateResponse(updatedUser);
    }

    // 회원 탈퇴 로직
    @Transactional
    public void deleteUser(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.USER_ALREADY_DELETED);
        }

        user.delete();
    }
}