package com.onlineshop.config;

import com.onlineshop.model.User;
import com.onlineshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@shop.com")) {
            User admin = User.builder()
                    .name("Admin")
                    .email("admin@shop.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(User.Role.ADMIN)
                    .build();
            userRepository.save(admin);
            System.out.println("Admin seeded: admin@shop.com / admin123");
        }
    }
}