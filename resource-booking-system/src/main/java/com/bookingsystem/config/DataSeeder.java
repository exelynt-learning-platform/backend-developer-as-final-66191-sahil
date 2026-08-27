package com.bookingsystem.config;

import com.bookingsystem.entity.Resource;
import com.bookingsystem.entity.Role;
import com.bookingsystem.entity.User;
import com.bookingsystem.repository.ResourceRepository;
import com.bookingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds baseline ADMIN/USER accounts and a handful of sample resources on startup,
 * so the API is immediately testable. Idempotent - only inserts if data is absent.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedResources();
    }

    private void seedUsers() {
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("Admin@123"))
                    .email("admin@bookingsystem.com")
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build());
            log.info("Seeded ADMIN user -> username: admin / password: Admin@123");
        }

        if (!userRepository.existsByUsername("user")) {
            userRepository.save(User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("User@123"))
                    .email("user@bookingsystem.com")
                    .role(Role.USER)
                    .enabled(true)
                    .build());
            log.info("Seeded USER user -> username: user / password: User@123");
        }

        if (!userRepository.existsByUsername("user2")) {
            userRepository.save(User.builder()
                    .username("user2")
                    .password(passwordEncoder.encode("User@123"))
                    .email("user2@bookingsystem.com")
                    .role(Role.USER)
                    .enabled(true)
                    .build());
            log.info("Seeded USER user -> username: user2 / password: User@123");
        }
    }

    private void seedResources() {
        if (resourceRepository.count() == 0) {
            resourceRepository.save(Resource.builder()
                    .name("Conference Room A")
                    .type("ROOM")
                    .description("Large conference room with projector and video conferencing.")
                    .location("Floor 3, Building 1")
                    .capacity(12)
                    .available(true)
                    .build());

            resourceRepository.save(Resource.builder()
                    .name("Conference Room B")
                    .type("ROOM")
                    .description("Small meeting room, ideal for 1:1s.")
                    .location("Floor 2, Building 1")
                    .capacity(4)
                    .available(true)
                    .build());

            resourceRepository.save(Resource.builder()
                    .name("Toyota Innova - MH12AB1234")
                    .type("VEHICLE")
                    .description("7-seater company vehicle for business travel.")
                    .location("Parking Lot 1")
                    .capacity(7)
                    .available(true)
                    .build());

            resourceRepository.save(Resource.builder()
                    .name("Projector EPX-200")
                    .type("EQUIPMENT")
                    .description("Portable HD projector.")
                    .location("Equipment Store Room")
                    .capacity(1)
                    .available(true)
                    .build());

            log.info("Seeded {} sample resources", resourceRepository.count());
        }
    }
}
