package com.example.resumeandportfolio.service.user;

import com.example.resumeandportfolio.model.entity.user.User;
import com.example.resumeandportfolio.model.enums.Role;
import com.example.resumeandportfolio.repository.user.UserRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Custom OAuth2 User Service Test
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DefaultOAuth2UserService defaultOAuth2UserService;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    private ClientRegistration clientRegistration;
    private OAuth2AccessToken accessToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock ClientRegistration
        clientRegistration = ClientRegistration.withRegistrationId("google")
            .clientId("test-client-id")
            .clientSecret("test-client-secret")
            .scope("email", "profile")
            .authorizationUri("https://accounts.google.com/o/oauth2/auth")
            .tokenUri("https://oauth2.googleapis.com/token")
            .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
            .userNameAttributeName("email")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/google")
            .clientName("Google")
            .build();

        // Mock OAuth2AccessToken
        accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "test-access-token",
            Instant.now(),
            Instant.now().plusSeconds(3600)
        );
    }

    @Test
    @DisplayName("사용자가 이미 존재할 경우 사용자 로드")
    void loadUser_WhenUserExists() {
        // given: 테스트 데이터와 의존성 설정
        String email = "test@example.com";
        User mockUser = User.builder()
            .email(email)
            .nickname("TestUser")
            .password("")
            .role(Role.VISITOR)
            .build();

        when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(mockUser));

        OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, accessToken);
        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            Collections.singleton(() -> "ROLE_USER"),
            Collections.singletonMap("email", email),
            "email"
        );

        when(defaultOAuth2UserService.loadUser(any(OAuth2UserRequest.class))).thenReturn(
            mockOAuth2User);

        // when: 서비스 호출
        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        // then: 결과 검증
        assertNotNull(result);
        assertEquals(email, result.getAttribute("email"));
        verify(userRepository, times(1)).findByEmailAndDeletedAtIsNull(email);
    }

    @Test
    @DisplayName("사용자가 존재하지 않을 경우 회원가입 후 로드")
    void loadUser_WhenUserDoesNotExist() {
        // given: 테스트 데이터와 의존성 설정
        String email = "newuser@example.com";
        String name = "NewUser";

        when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.empty());

        User newUser = User.builder()
            .email(email)
            .nickname(name)
            .password("")
            .role(Role.VISITOR)
            .build();
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, accessToken);
        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            Collections.singleton(() -> "ROLE_USER"),
            Collections.singletonMap("email", email),
            "email"
        );

        when(defaultOAuth2UserService.loadUser(any(OAuth2UserRequest.class))).thenReturn(
            mockOAuth2User);

        // when: 서비스 호출
        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        // then: 결과 검증
        assertNotNull(result);
        assertEquals(email, result.getAttribute("email"));
        verify(userRepository, times(1)).findByEmailAndDeletedAtIsNull(email);
        verify(userRepository, times(1)).save(any(User.class));
    }
}