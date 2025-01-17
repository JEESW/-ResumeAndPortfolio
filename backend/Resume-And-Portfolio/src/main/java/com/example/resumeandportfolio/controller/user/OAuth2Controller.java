package com.example.resumeandportfolio.controller.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
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

    @GetMapping("/google")
    public ResponseEntity<String> redirectToGoogleOAuth() {
        String redirectUrl =
            OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
                + "/google";
        return ResponseEntity.ok(redirectUrl);
    }
}