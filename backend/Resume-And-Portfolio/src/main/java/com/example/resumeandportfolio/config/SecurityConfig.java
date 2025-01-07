package com.example.resumeandportfolio.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Config
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    // 시큐리티 체인
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/api/users/login", "/api/users/join").permitAll()
                .anyRequest().authenticated()
            )
            .logout(logout -> logout
                .logoutUrl("/api/users/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(200); // 로그아웃 성공 시 HTTP 200 반환
                })
                .invalidateHttpSession(true) // 세션 무효화
                .deleteCookies("JSESSIONID") // 쿠키 삭제
            );

        return http.build();
    }

    // 패스워드 인코더
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}