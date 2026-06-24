package com.novacart.store.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class AuthDtos {

    private AuthDtos() {}

    public record SignupRequest(
            @Email @NotBlank @Size(max = 180) String email,
            @NotBlank @Size(min = 2, max = 80) String displayName,
            @NotBlank @Size(min = 8, max = 80) String password,
            @Size(max = 120) String location
    ) {}

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record AuthResponse(
            Instant expiresAt,
            UserDtos.UserSummary user
    ) {}
}
