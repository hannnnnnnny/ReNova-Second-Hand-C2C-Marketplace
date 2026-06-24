package com.novacart.store.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class CsrfCookieIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void csrfCookieAndHeaderProtectTheRealBrowserSignupAndLogoutFlow() throws Exception {
        MvcResult tokenResult = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isString())
                .andReturn();

        Cookie csrfCookie = tokenResult.getResponse().getCookie("XSRF-TOKEN");
        assertThat(csrfCookie).isNotNull();
        assertThat(csrfCookie.isHttpOnly()).isFalse();
        assertThat(csrfCookie.getAttribute("SameSite")).isEqualTo("Lax");
        String csrfToken = csrfCookie.getValue();

        String email = "browser." + UUID.randomUUID() + "@example.test";
        MvcResult signupResult = mockMvc.perform(post("/api/auth/signup")
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "displayName", "Browser Cookie User",
                                "password", "T!" + UUID.randomUUID() + "a1",
                                "location", "Test City"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").doesNotExist())
                .andReturn();

        Cookie sessionCookie = signupResult.getResponse().getCookie(SessionCookieService.COOKIE_NAME);
        assertThat(sessionCookie).isNotNull();
        assertThat(sessionCookie.isHttpOnly()).isTrue();
        assertThat(sessionCookie.getAttribute("SameSite")).isEqualTo("Lax");

        mockMvc.perform(get("/api/auth/me").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email));

        MvcResult logoutResult = mockMvc.perform(post("/api/auth/logout")
                        .cookie(sessionCookie, csrfCookie)
                        .header("X-XSRF-TOKEN", csrfToken))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(logoutResult.getResponse().getHeader(HttpHeaders.SET_COOKIE))
                .contains(SessionCookieService.COOKIE_NAME + "=")
                .contains("Max-Age=0")
                .contains("HttpOnly")
                .contains("SameSite=Lax");
    }
}
