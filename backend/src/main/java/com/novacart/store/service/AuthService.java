package com.novacart.store.service;

import com.novacart.store.dto.AuthDtos;
import com.novacart.store.dto.UserDtos;
import com.novacart.store.entity.User;
import com.novacart.store.entity.UserStatus;
import com.novacart.store.exception.AuthenticationFailedException;
import com.novacart.store.exception.DuplicateResourceException;
import com.novacart.store.repository.UserRepository;
import com.novacart.store.security.JwtService;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthDtos.AuthResponse signup(AuthDtos.SignupRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("An account with this email already exists.");
        }
        User user = new User();
        user.setEmail(email);
        user.setDisplayName(request.displayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setLocation(request.location());
        user.setCreatedAt(Instant.now());
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        return buildResponse(user);
    }

    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new AuthenticationFailedException("Email or password is incorrect."));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AuthenticationFailedException("This account is not active.");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationFailedException("Email or password is incorrect.");
        }
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        return buildResponse(user);
    }

    private AuthDtos.AuthResponse buildResponse(User user) {
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthDtos.AuthResponse(
                token,
                jwtService.getExpirationMinutes(),
                UserDtos.UserSummary.from(user)
        );
    }
}
