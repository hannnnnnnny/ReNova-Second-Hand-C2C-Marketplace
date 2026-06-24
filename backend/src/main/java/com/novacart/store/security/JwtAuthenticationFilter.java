package com.novacart.store.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AppUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            AppUserDetailsService userDetailsService
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = readSessionToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            JwtTokenValidation validation = jwtService.validateToken(token);

            if (validation.status() == JwtTokenStatus.VALID) {
                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(validation.subject());
                    if (jwtService.isTokenValid(token, userDetails.getUsername()) && userDetails.isEnabled()) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (UsernameNotFoundException ignored) {
                    // fall through to anonymous; protected endpoints will return 401 via security config
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String readSessionToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> SessionCookieService.COOKIE_NAME.equals(cookie.getName()))
                .map(jakarta.servlet.http.Cookie::getValue)
                .filter(value -> !value.isBlank())
                .findFirst()
                .orElse(null);
    }
}
