package com.novacart.store.security;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class SessionCookieService {

    public static final String COOKIE_NAME = "RENOVA_SESSION";

    private final Duration sessionDuration;
    private final boolean secure;

    public SessionCookieService(
            @Value("${novacart.security.jwt-expiration-minutes}") long expirationMinutes,
            @Value("${novacart.security.cookie-secure}") boolean secure
    ) {
        this.sessionDuration = Duration.ofMinutes(expirationMinutes);
        this.secure = secure;
    }

    public ResponseCookie create(String token) {
        return base(token)
                .maxAge(sessionDuration)
                .build();
    }

    public ResponseCookie expire() {
        return base("")
                .maxAge(Duration.ZERO)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/api");
    }
}
