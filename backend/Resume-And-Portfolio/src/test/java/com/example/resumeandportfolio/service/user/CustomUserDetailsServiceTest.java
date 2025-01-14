package com.example.resumeandportfolio.service.user;

import com.example.resumeandportfolio.exception.CustomException;
import com.example.resumeandportfolio.exception.ErrorCode;
import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.model.enums.Role;
import com.example.resumeandportfolio.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Custom User Details Service Test
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = User.builder()
            .email("test@example.com")
            .password("encoded_password")
            .nickname("Tester")
            .role(Role.VISITOR)
            .build();
    }

    @Test
    @DisplayName("사용자 검색 성공 테스트 - 유효한 이메일로 사용자 검색")
    void loadUserByUsername_success() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmailAndDeletedAtIsNull(email))
            .thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getPassword()).isEqualTo(testUser.getPassword());
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo(
            Role.VISITOR.name());

        // Verify
        verify(userRepository, times(1)).findByEmailAndDeletedAtIsNull(email);
    }

    @Test
    @DisplayName("사용자 검색 실패 테스트 - 사용자 없음")
    void loadUserByUsername_userNotFound() {
        // Given
        String email = "notfound@example.com";
        when(userRepository.findByEmailAndDeletedAtIsNull(email))
            .thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
            customUserDetailsService.loadUserByUsername(email)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);

        // Verify
        verify(userRepository, times(1)).findByEmailAndDeletedAtIsNull(email);
    }
}