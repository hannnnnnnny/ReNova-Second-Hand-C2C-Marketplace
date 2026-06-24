package com.novacart.store.config;

import com.novacart.store.dto.ErrorResponse;
import com.novacart.store.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ObjectMapper objectMapper,
            CookieCsrfTokenRepository csrfTokenRepository
    ) throws Exception {
        return http
                .csrf(csrf -> csrf
                        .spa()
                        .csrfTokenRepository(csrfTokenRepository)
                )
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> writeSecurityError(
                                objectMapper,
                                response,
                                request.getRequestURI(),
                                HttpStatus.UNAUTHORIZED,
                                "Authentication is required."
                        ))
                        .accessDeniedHandler((request, response, exception) -> writeSecurityError(
                                objectMapper,
                                response,
                                request.getRequestURI(),
                                HttpStatus.FORBIDDEN,
                                "You do not have permission to perform this action."
                        ))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/csrf", "/api/public/**", "/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CookieCsrfTokenRepository csrfTokenRepository(
            @Value("${novacart.security.cookie-secure}") boolean secure
    ) {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(cookie -> cookie
                .path("/")
                .secure(secure)
                .sameSite("Lax"));
        return repository;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${novacart.cors.allowed-origins}") String allowedOrigins
    ) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(parseOrigins(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Accept", "Content-Type", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private List<String> parseOrigins(String allowedOrigins) {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
    }

    private void writeSecurityError(
            ObjectMapper objectMapper,
            HttpServletResponse response,
            String path,
            HttpStatus status,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ErrorResponse.of(message, status.value(), path));
    }
}
