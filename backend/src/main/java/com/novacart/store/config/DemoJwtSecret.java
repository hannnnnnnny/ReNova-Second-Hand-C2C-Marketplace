package com.novacart.store.config;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import java.util.Map;

/**
 * Generates a fresh random JWT secret per JVM when the {@code demo}
 * profile is active and the operator did not explicitly set
 * {@code JWT_SECRET}. This lets {@code java -jar … --spring.profiles.active=demo}
 * run a self-contained showcase without anyone ever copy-pasting a
 * stable placeholder secret into a real environment.
 *
 * <p>Registered via {@code META-INF/spring.factories}.
 */
public class DemoJwtSecret implements EnvironmentPostProcessor {

    private static final String PROPERTY = "novacart.security.jwt-secret";
    private static final String ENV_VAR = "JWT_SECRET";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication application) {
        boolean demo = false;
        for (String p : env.getActiveProfiles()) {
            if ("demo".equals(p)) { demo = true; break; }
        }
        if (!demo) {
            return;
        }
        // Honour an explicit operator-supplied secret if present.
        if (env.containsProperty(ENV_VAR) && !isBlank(env.getProperty(ENV_VAR))) {
            return;
        }

        byte[] raw = new byte[48]; // 384 bits, well above HS256 minimum
        new SecureRandom().nextBytes(raw);
        String generated = Base64.getEncoder().withoutPadding().encodeToString(raw);

        env.getPropertySources().addFirst(new MapPropertySource(
                "renova-demo-jwt-secret",
                Map.of(PROPERTY, generated)
        ));
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
