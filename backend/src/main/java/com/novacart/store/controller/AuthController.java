package com.novacart.store.controller;

import com.novacart.store.dto.ApiResponse;
import com.novacart.store.dto.AuthDtos;
import com.novacart.store.dto.UserDtos;
import com.novacart.store.security.CurrentUserService;
import com.novacart.store.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

    public AuthController(AuthService authService, CurrentUserService currentUserService) {
        this.authService = authService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> signup(@Valid @RequestBody AuthDtos.SignupRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Welcome to ReNova.", authService.signup(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Signed in.", authService.login(request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDtos.UserSummary>> me() {
        return ResponseEntity.ok(ApiResponse.success("Current session.", UserDtos.UserSummary.from(currentUserService.requireCurrentUser())));
    }
}
