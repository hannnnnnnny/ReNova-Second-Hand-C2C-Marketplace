package com.novacart.store.controller;

import com.novacart.store.dto.ApiResponse;
import com.novacart.store.dto.AuthDtos;
import com.novacart.store.dto.UserDtos;
import com.novacart.store.security.CurrentUserService;
import com.novacart.store.security.SessionCookieService;
import com.novacart.store.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CurrentUserService currentUserService;
    private final SessionCookieService sessionCookieService;

    public AuthController(
            AuthService authService,
            CurrentUserService currentUserService,
            SessionCookieService sessionCookieService
    ) {
        this.authService = authService;
        this.currentUserService = currentUserService;
        this.sessionCookieService = sessionCookieService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> signup(@Valid @RequestBody AuthDtos.SignupRequest request) {
        return authenticatedResponse("Welcome to ReNova.", authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return authenticatedResponse("Signed in.", authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessionCookieService.expire().toString())
                .body(ApiResponse.success("Signed out."));
    }

    @GetMapping("/csrf")
    public ResponseEntity<ApiResponse<Map<String, String>>> csrf(CsrfToken token) {
        return ResponseEntity.ok(ApiResponse.success("CSRF token issued.", Map.of("token", token.getToken())));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDtos.UserSummary>> me() {
        return ResponseEntity.ok(ApiResponse.success("Current session.", UserDtos.UserSummary.from(currentUserService.requireCurrentUser())));
    }

    private ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> authenticatedResponse(
            String message,
            AuthService.AuthenticatedSession session
    ) {
        AuthDtos.AuthResponse body = new AuthDtos.AuthResponse(session.expiresAt(), session.user());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessionCookieService.create(session.token()).toString())
                .body(ApiResponse.success(message, body));
    }
}
