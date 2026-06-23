package com.novacart.store.config;

import com.novacart.store.entity.UserStatus;
import com.novacart.store.repository.UserRepository;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!demo")
public class KnownCredentialAccountRemediator implements ApplicationRunner {

    private static final List<String> LEGACY_DEMO_EMAILS = List.of(
            "admin@renova.local",
            "ava@renova.local",
            "liam@renova.local",
            "nora@renova.local",
            "sam@renova.local"
    );

    private final UserRepository userRepository;

    public KnownCredentialAccountRemediator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        LEGACY_DEMO_EMAILS.stream()
                .map(userRepository::findByEmailIgnoreCase)
                .flatMap(java.util.Optional::stream)
                .forEach(user -> user.setStatus(UserStatus.DEACTIVATED));
    }
}
