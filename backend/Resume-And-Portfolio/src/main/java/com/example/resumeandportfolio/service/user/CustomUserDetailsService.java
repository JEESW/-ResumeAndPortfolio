package com.example.resumeandportfolio.service.user;

import com.example.resumeandportfolio.exception.CustomException;
import com.example.resumeandportfolio.exception.ErrorCode;
import java.util.List;
import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 데이터베이스에서 사용자 검색
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // UserDetails 객체 생성 및 반환
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            List.of(new SimpleGrantedAuthority(user.getRole().name())) // 권한 설정
        );
    }
}