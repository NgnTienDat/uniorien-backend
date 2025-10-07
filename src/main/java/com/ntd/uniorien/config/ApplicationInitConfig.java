package com.ntd.uniorien.config;

import com.ntd.uniorien.constant.PredefinedRole;
import com.ntd.uniorien.entity.Role;
import com.ntd.uniorien.entity.User;
import com.ntd.uniorien.repository.RoleRepository;
import com.ntd.uniorien.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;

    @NonFinal
    @Value("${app.admin.password}")
    String adminPassword;

    @NonFinal
    @Value("${app.admin.email}")
    String adminEmail;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        log.info("Initializing application.....");
        return args -> {
            if (userRepository.findUserByEmail(adminEmail).isEmpty()) {

                // crate roles
                roleRepository.save(Role.builder()
                        .roleName(PredefinedRole.USER_ROLE)
                        .description("User role")
                        .build());

                Role adminRole = roleRepository.save(Role.builder()
                        .roleName(PredefinedRole.ADMIN_ROLE)
                        .description("Admin role")
                        .build());

                // create admin user
                var roles = new HashSet<Role>();
                roles.add(adminRole);

                User admin = User.builder()
                        .roles(roles)
                        .email(adminEmail)
                        .fullName("System Admin")
                        .password(passwordEncoder.encode(adminPassword))
                        .build();

                userRepository.save(admin);
                log.info("Created admin user");
            }
        };
    }
}
