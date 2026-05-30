package com.novacart.store.security;

import com.novacart.store.entity.User;
import com.novacart.store.exception.AuthenticationFailedException;
import com.novacart.store.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Resolve the JWT-authenticated user, or fail with 401.
     *
     * <p>Both "no authentication present" and Spring's default
     * AnonymousAuthenticationToken are treated as unauthenticated.
     * If we accepted the anonymous principal we would 404 on the
     * follow-up DB lookup (no user is named "anonymousUser"), which
     * would leak whether a route exists vs whether the caller has
     * access. This method always 401s instead.
     */
    public User requireCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(auth)) {
            throw new AuthenticationFailedException("Authentication is required.");
        }
        String email = auth.getName();
        return userRepository.findByEmailIgnoreCase(email)
                // The token verified, but the user row was deleted under us.
                // Treat as auth failure, not as "resource not found".
                .orElseThrow(() -> new AuthenticationFailedException("Authentication is required."));
    }

    public User getCurrentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(auth)) {
            return null;
        }
        return userRepository.findByEmailIgnoreCase(auth.getName()).orElse(null);
    }

    private boolean isAuthenticated(Authentication auth) {
        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)
                && auth.getName() != null
                && !auth.getName().isBlank();
    }
}
