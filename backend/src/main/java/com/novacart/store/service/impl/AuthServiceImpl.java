package com.novacart.store.service.impl;

import com.novacart.store.dto.LoginRequest;
import com.novacart.store.dto.LoginResponse;
import com.novacart.store.entity.AdminUser;
import com.novacart.store.exception.AuthenticationFailedException;
import com.novacart.store.repository.AdminUserRepository;
import com.novacart.store.security.JwtService;
import com.novacart.store.service.AuthService;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final Duration LOGIN_LOCK_WINDOW = Duration.ofMinutes(15);
    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email address or password.";
    private static final String RATE_LIMIT_MESSAGE = "Too many failed login attempts. Try again in 15 minutes.";

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Map<String, FailedLoginWindow> failedLogins = new ConcurrentHashMap<>();

    public AuthServiceImpl(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        rejectIfRateLimited(normalizedEmail);

        AdminUser adminUser = adminUserRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(AdminUser::isActive)
                .orElse(null);

        if (adminUser == null || !passwordEncoder.matches(request.password(), adminUser.getPasswordHash())) {
            recordFailedLogin(normalizedEmail);
            throw new AuthenticationFailedException(INVALID_CREDENTIALS_MESSAGE);
        }

        failedLogins.remove(normalizedEmail);
        String token = jwtService.generateToken(adminUser.getEmail(), adminUser.getRole());
        return new LoginResponse(token, "Bearer", jwtService.getExpirationMinutes(), adminUser.getEmail(), adminUser.getRole());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void rejectIfRateLimited(String email) {
        FailedLoginWindow window = failedLogins.get(email);
        if (window == null) {
            return;
        }

        if (window.isExpired()) {
            failedLogins.remove(email);
            return;
        }

        if (window.attempts() >= MAX_FAILED_LOGIN_ATTEMPTS) {
            throw new AuthenticationFailedException(RATE_LIMIT_MESSAGE);
        }
    }

    private void recordFailedLogin(String email) {
        Instant now = Instant.now();
        failedLogins.compute(email, (key, window) -> {
            if (window == null || window.isExpired(now)) {
                return new FailedLoginWindow(1, now);
            }
            return new FailedLoginWindow(window.attempts() + 1, window.firstAttemptAt());
        });
    }

    private record FailedLoginWindow(int attempts, Instant firstAttemptAt) {
        boolean isExpired() {
            return isExpired(Instant.now());
        }

        boolean isExpired(Instant now) {
            return firstAttemptAt.plus(LOGIN_LOCK_WINDOW).isBefore(now);
        }
    }
}
