
package com.peoplepulse.config;

import com.peoplepulse.model.User;
import com.peoplepulse.repo.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeeder {

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Bean
    CommandLineRunner createInitialAdmin(
            UserRepository users,
            PasswordEncoder passwordEncoder) {

        return args -> {
            if (adminEmail.isBlank() || adminPassword.isBlank()) {
                System.out.println("Admin account not configured. Skipping creation.");
                return;
            }

            if (adminPassword.length() < 12) {
                throw new IllegalStateException(
                        "Initial admin password must be at least 12 characters.");
            }

            String email = adminEmail.trim();

            if (!users.existsByEmailIgnoreCase(email)) {
                User admin = new User();
                admin.setEmail(email);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole("ADMIN");
                admin.setEmployeeId(null);
                admin.setEnabled(true);

                users.save(admin);
                System.out.println("Initial Admin account created.");
            }
        };
    }
}
