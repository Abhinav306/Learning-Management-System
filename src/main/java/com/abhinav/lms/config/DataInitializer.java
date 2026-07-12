package com.abhinav.lms.config;

import com.abhinav.lms.user.entity.User;
import com.abhinav.lms.user.entity.UserRole;
import com.abhinav.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        bootstrapAdminUser();
    }

    private void bootstrapAdminUser() {
        String adminEmail = "admin@lms.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            log.info("Bootstrapping default admin user: {}", adminEmail);
            User admin = User.builder()
                    .firstName("System")
                    .lastName("Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin12345"))
                    .role(UserRole.ADMIN)
                    .enabled(true)
                    .emailVerified(true)
                    .build();
            userRepository.save(admin);
            log.info("Default admin user bootstrapped successfully");
        } else {
            log.debug("Admin user already exists, skipping bootstrap");
        }
    }
}
