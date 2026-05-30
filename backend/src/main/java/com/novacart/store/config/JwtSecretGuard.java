package com.novacart.store.config;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Refuses to start the application if {@code JWT_SECRET} is missing,
 * obviously a placeholder, or too short.
 *
 * <p>Active in every profile except {@code dev}, {@code test}, and
 * {@code demo}. Those three are allowed to fall through to an
 * application-supplied secret because they are explicitly local-only
 * (see {@link DemoJwtSecret} for the random-per-boot demo case).
 */
@Component
@Profile("!dev & !test & !demo")
public class JwtSecretGuard {

    /**
     * Substrings that mark a secret as a known placeholder leaked into
     * source. Any match aborts startup. Lower-cased before comparing.
     */
    private static final List<String> KNOWN_PLACEHOLDER_TOKENS = List.of(
            "change-this",
            "change_this",
            "replace-with",
            "replace_with",
            "not-for-production",
            "for-local",
            "demo-secret",
            "your-secret-here",
            "__set_me__"
    );

    private static final int MIN_SECRET_BYTES = 32;

    private final String secret;
    private final Environment environment;

    public JwtSecretGuard(
            @Value("${novacart.security.jwt-secret:}") String secret,
            Environment environment
    ) {
        this.secret = secret;
        this.environment = environment;
    }

    @PostConstruct
    void verify() {
        String activeProfiles = String.join(",", environment.getActiveProfiles());
        if (secret == null || secret.isBlank()) {
            fail("JWT_SECRET is not set. Refusing to start in profile(s) [" + activeProfiles
                    + "]. Set the JWT_SECRET environment variable to a value of at least "
                    + MIN_SECRET_BYTES + " bytes.");
        }
        String lower = secret.toLowerCase();
        for (String token : KNOWN_PLACEHOLDER_TOKENS) {
            if (lower.contains(token)) {
                fail("JWT_SECRET is set to a known placeholder ('" + token
                        + "'). Refusing to start in profile(s) [" + activeProfiles
                        + "]. Generate a real secret (32+ random bytes).");
            }
        }
        int bytes = secret.getBytes(StandardCharsets.UTF_8).length;
        if (bytes < MIN_SECRET_BYTES) {
            fail("JWT_SECRET is too short (" + bytes + " bytes). Minimum is "
                    + MIN_SECRET_BYTES + " bytes (HS256 key length).");
        }
        // Defense in depth: low-entropy values like a single repeated
        // character. Reject if the distinct-byte ratio is suspiciously low.
        long distinct = Arrays.stream(secret.codePoints().toArray()).distinct().count();
        if (distinct < 8) {
            fail("JWT_SECRET has very low entropy (" + distinct
                    + " distinct characters). Generate a random secret.");
        }
    }

    private void fail(String message) {
        throw new IllegalStateException(
                "GATE 3 (no secrets in git) check failed: " + message);
    }
}
