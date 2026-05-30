package com.novacart.store.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.novacart.store.config.JwtSecretGuard;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

/**
 * GATE 3 evidence: the application refuses to boot with a missing,
 * placeholder, or low-entropy JWT secret in production-shaped profiles.
 *
 * <p>Unit-level — invokes JwtSecretGuard.verify() directly, no Spring
 * context, so the test asserts the rejection contract independent of
 * profile wiring.
 */
class JwtSecretGuardTests {

    private static JwtSecretGuard guardWith(String secret) {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        return new JwtSecretGuard(secret, env);
    }

    private static void invokeVerify(JwtSecretGuard guard) throws Exception {
        // package-private; invoke directly via reflection-free same-package call
        var method = JwtSecretGuard.class.getDeclaredMethod("verify");
        method.setAccessible(true);
        try {
            method.invoke(guard);
        } catch (java.lang.reflect.InvocationTargetException wrap) {
            if (wrap.getCause() instanceof RuntimeException re) throw re;
            throw wrap;
        }
    }

    @Test
    void rejectsMissingSecret() {
        assertThatThrownBy(() -> invokeVerify(guardWith(null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET is not set");

        assertThatThrownBy(() -> invokeVerify(guardWith("")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET is not set");

        assertThatThrownBy(() -> invokeVerify(guardWith("   ")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET is not set");
    }

    @Test
    void rejectsKnownPlaceholderStrings() throws Exception {
        // Every literal that was previously baked into committed files.
        String[] placeholders = {
                "change-this-development-secret-to-a-long-random-value",
                "renova-demo-secret-not-for-production-use-only",
                "replace-with-a-long-random-secret-for-local-docker-only",
                "replace-with-a-long-random-secret-for-local-development",
                "__SET_ME__",
                "your-secret-here-is-long-enough-but-still-a-placeholder",
                "demo-secret-please-do-not-deploy-with-this-value"
        };
        for (String bad : placeholders) {
            assertThatThrownBy(() -> invokeVerify(guardWith(bad)))
                    .as("placeholder should be rejected: %s", bad)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("placeholder");
        }
    }

    @Test
    void rejectsTooShortSecret() {
        assertThatThrownBy(() -> invokeVerify(guardWith("abcdefghij1234567890")))   // 20 bytes
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too short");
    }

    @Test
    void rejectsLowEntropySecret() {
        // 64 chars but only 1 distinct character.
        String low = "a".repeat(64);
        assertThatThrownBy(() -> invokeVerify(guardWith(low)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("low entropy");
    }

    @Test
    void acceptsRealLookingSecret() throws Exception {
        // 48 random-looking bytes, 8+ distinct chars, no placeholder substring.
        String good = "k9mP2wQ7vT5xB8nL3jR4hF6yD1sZaUcEiO0qN!aV";
        invokeVerify(guardWith(good));
        // No exception thrown — verified by reaching this line.
        assertThat(true).isTrue();
    }
}
