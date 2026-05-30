package com.novacart.store.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.novacart.store.dto.AuthDtos;
import com.novacart.store.entity.User;
import com.novacart.store.exception.AuthenticationFailedException;
import com.novacart.store.exception.DuplicateResourceException;
import com.novacart.store.repository.UserRepository;
import com.novacart.store.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

/**
 * GATE 1 evidence: passwords are never stored or returned in plaintext.
 *
 * <p>This is not a smoke test. Each assertion is the literal acceptance
 * criterion for the gate. If any of these would ever start failing,
 * GATE 1 has regressed.
 */
@SpringBootTest
@ActiveProfiles("test")
class PasswordSecurityTests {

    private static final String PLAIN_PASSWORD = "CorrectHorseBatteryStaple9!";

    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void signup_storesBcryptHashNotPlaintext() {
        authService.signup(new AuthDtos.SignupRequest(
                "alice@gate1.test", "Alice", PLAIN_PASSWORD, "Auckland, NZ"));

        User saved = userRepository.findByEmailIgnoreCase("alice@gate1.test").orElseThrow();

        // The stored value must NOT be the plaintext.
        assertThat(saved.getPasswordHash()).isNotEqualTo(PLAIN_PASSWORD);

        // The stored value must be a BCrypt hash: $2a$/$2b$/$2y$, 60 chars total.
        assertThat(saved.getPasswordHash())
                .matches("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$")
                .hasSize(60);

        // BCrypt encoder must accept the original plaintext against the stored hash.
        assertThat(passwordEncoder.matches(PLAIN_PASSWORD, saved.getPasswordHash())).isTrue();

        // And reject any different password.
        assertThat(passwordEncoder.matches("WrongPassword!", saved.getPasswordHash())).isFalse();
    }

    @Test
    void signupResponse_doesNotSerializePasswordOrHash() throws Exception {
        AuthDtos.AuthResponse response = authService.signup(new AuthDtos.SignupRequest(
                "bob@gate1.test", "Bob", PLAIN_PASSWORD, null));

        String json = objectMapper.writeValueAsString(response);

        // Neither the plaintext nor any password-related field name may appear.
        assertThat(json).doesNotContain(PLAIN_PASSWORD);
        assertThat(json.toLowerCase()).doesNotContain("password");
        assertThat(json.toLowerCase()).doesNotContain("passwordhash");
        assertThat(json.toLowerCase()).doesNotContain("password_hash");
        // The token does need to be there.
        assertThat(json).contains("\"token\"");
    }

    @Test
    void login_rejectsWrongPasswordWithGenericMessage() {
        authService.signup(new AuthDtos.SignupRequest(
                "carol@gate1.test", "Carol", PLAIN_PASSWORD, null));

        // Right password works.
        assertThat(authService.login(new AuthDtos.LoginRequest("carol@gate1.test", PLAIN_PASSWORD)))
                .isNotNull();

        // Wrong password rejected. Message must NOT distinguish "no such user"
        // vs "bad password" (prevents user enumeration).
        assertThatThrownBy(() ->
                authService.login(new AuthDtos.LoginRequest("carol@gate1.test", "Wrong!")))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Email or password is incorrect.");

        // Same message for a non-existent account.
        assertThatThrownBy(() ->
                authService.login(new AuthDtos.LoginRequest("nobody@gate1.test", "Whatever1!")))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Email or password is incorrect.");
    }

    @Test
    void signup_rejectsDuplicateEmail_caseInsensitively() {
        authService.signup(new AuthDtos.SignupRequest(
                "dup@gate1.test", "First", PLAIN_PASSWORD, null));

        assertThatThrownBy(() -> authService.signup(new AuthDtos.SignupRequest(
                "DUP@gate1.test", "Second", PLAIN_PASSWORD, null)))
                .isInstanceOf(DuplicateResourceException.class);
    }
}
