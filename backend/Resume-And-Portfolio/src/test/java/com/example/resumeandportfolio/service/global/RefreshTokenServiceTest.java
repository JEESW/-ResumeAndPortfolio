package com.example.resumeandportfolio.service.global;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

/**
 * Refresh Token Service Test
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

class RefreshTokenServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Refresh Token 저장 테스트 - Redis에 성공적으로 저장")
    void saveRefreshToken_shouldStoreTokenInRedis() {
        // Given
        String username = "user@example.com";
        String refreshToken = "sampleRefreshToken";
        long duration = 3600L;

        // When
        refreshTokenService.saveRefreshToken(username, refreshToken, duration);

        // Then
        verify(valueOperations).set("refresh:" + username, refreshToken, duration,
            TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Refresh Token 조회 테스트 - Redis에서 성공적으로 조회")
    void getRefreshToken_shouldRetrieveTokenFromRedis() {
        // Given
        String username = "user@example.com";
        String refreshToken = "sampleRefreshToken";

        when(valueOperations.get("refresh:" + username)).thenReturn(refreshToken);

        // When
        String result = refreshTokenService.getRefreshToken(username);

        // Then
        assertThat(result).isEqualTo(refreshToken);
        verify(valueOperations).get("refresh:" + username);
    }

    @Test
    @DisplayName("Refresh Token 삭제 테스트 - Redis에서 성공적으로 삭제")
    void deleteRefreshToken_shouldRemoveTokenFromRedis() {
        // Given
        String username = "user@example.com";

        // When
        refreshTokenService.deleteRefreshToken(username);

        // Then
        verify(redisTemplate).delete("refresh:" + username);
    }
}