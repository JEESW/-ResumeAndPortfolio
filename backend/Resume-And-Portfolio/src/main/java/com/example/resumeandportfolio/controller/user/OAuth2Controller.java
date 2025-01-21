package com.example.resumeandportfolio.controller.user;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OAuth2 Controller
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@RestController
@RequestMapping("/api/users/oauth")
public class OAuth2Controller {

    // Google 인증 URL로 리다이렉트
    @GetMapping("/google")
    public void redirectToGoogleOAuth(HttpServletResponse response) throws IOException {
        String redirectUrl = "https://www.jsw-resumeandportfolio.com/oauth2/authorization/google";
        response.sendRedirect(redirectUrl);
    }
}