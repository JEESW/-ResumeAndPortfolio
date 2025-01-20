package com.example.resumeandportfolio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS MVC Configuration
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/api/**")
            .exposedHeaders("Authorization", "Set-Cookie")
            .allowedOrigins("https://www.jsw-resumeandportfolio.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowCredentials(true);
    }
}