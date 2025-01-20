package com.example.resumeandportfolio.controller.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

/**
 * OAuth2 Controller Test
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@WebMvcTest(OAuth2Controller.class)
class OAuth2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("구글 OAuth 리다이렉트 URL 테스트")
    void redirectToGoogleOAuthTest() throws Exception {
        mockMvc.perform(get("/api/users/oauth/google"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "http://localhost/oauth2/authorization/google"));
    }
}