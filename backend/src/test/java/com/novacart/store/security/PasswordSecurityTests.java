package com.novacart.store.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.novacart.store.config.KnownCredentialAccountRemediator;
import com.novacart.store.entity.User;
import com.novacart.store.entity.UserStatus;
import com.novacart.store.repository.UserRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
class PasswordSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KnownCredentialAccountRemediator knownCredentialAccountRemediator;

    @Test
    void nonDemoProfileDoesNotCreateKnownCredentialAccounts() {
        assertThat(userRepository.findByEmailIgnoreCase("admin@renova.local")).isEmpty();
        assertThat(userRepository.findByEmailIgnoreCase("ava@renova.local")).isEmpty();
    }

    @Test
    void legacyKnownCredentialAccountIsDeactivatedAndCannotLogin() throws Exception {
        String password = strongPassword();
        User legacyUser = new User();
        legacyUser.setEmail("liam@renova.local");
        legacyUser.setDisplayName("Legacy Demo User");
        legacyUser.setPasswordHash(passwordEncoder.encode(password));
        legacyUser.setCreatedAt(Instant.now());
        legacyUser.setStatus(UserStatus.ACTIVE);
        userRepository.saveAndFlush(legacyUser);

        knownCredentialAccountRemediator.run(null);

        User remediated = userRepository.findByEmailIgnoreCase(legacyUser.getEmail()).orElseThrow();
        assertThat(remediated.getStatus()).isEqualTo(UserStatus.DEACTIVATED);
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", legacyUser.getEmail(),
                                "password", password
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrationStoresOnlyBcryptAndDoesNotExposeOrLogPassword(CapturedOutput output) throws Exception {
        String email = uniqueEmail("signup");
        String password = strongPassword();

        MvcResult result = signup(email, password)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.user.password").doesNotExist())
                .andExpect(jsonPath("$.data.user.passwordHash").doesNotExist())
                .andReturn();

        User stored = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(stored.getPasswordHash())
                .isNotEqualTo(password)
                .startsWith("$2");
        assertThat(passwordEncoder.matches(password, stored.getPasswordHash())).isTrue();
        assertThat(result.getResponse().getContentAsString()).doesNotContain(password);
        assertThat(objectMapper.writeValueAsString(stored))
                .doesNotContain(password)
                .doesNotContain(stored.getPasswordHash())
                .doesNotContain("passwordHash");
        assertThat(output.getAll()).doesNotContain(password);
    }

    @Test
    void loginUsesStoredHashAndDoesNotExposeOrLogPassword(CapturedOutput output) throws Exception {
        String email = uniqueEmail("login");
        String password = strongPassword();
        signup(email, password).andExpect(status().isOk());

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").doesNotExist())
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.user.password").doesNotExist())
                .andExpect(jsonPath("$.data.user.passwordHash").doesNotExist())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).doesNotContain(password);
        assertThat(output.getAll()).doesNotContain(password);
    }

    private org.springframework.test.web.servlet.ResultActions signup(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "email", email,
                        "displayName", "Security Test User",
                        "password", password,
                        "location", "Test City"
                ))));
    }

    private String uniqueEmail(String prefix) {
        return prefix + "." + UUID.randomUUID() + "@example.test";
    }

    private String strongPassword() {
        return "T!" + UUID.randomUUID() + "a1";
    }
}
