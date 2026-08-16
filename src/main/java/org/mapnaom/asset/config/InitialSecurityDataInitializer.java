package org.mapnaom.asset.config;

import org.mapnaom.asset.entity.AppUser;
import org.mapnaom.asset.entity.Role;
import org.mapnaom.asset.repository.AppUserRepository;
import org.mapnaom.asset.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;

@Component
public class InitialSecurityDataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public InitialSecurityDataInitializer(
            RoleRepository roleRepository,
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.initial-admin-username:}") String adminUsername,
            @Value("${app.security.initial-admin-password:}") String adminPassword
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        Role userRole = ensureRole("ROLE_USER");
        Role adminRole = ensureRole("ROLE_ADMIN");
        if (adminUsername.isBlank() && adminPassword.isBlank()) {
            return;
        }
        if (adminUsername.isBlank() || adminPassword.length() < 8) {
            throw new IllegalStateException("Initial admin username and a password of at least 8 characters must be configured together");
        }
        String username = adminUsername.trim().toLowerCase();
        if (userRepository.existsByUsername(username)) {
            return;
        }
        AppUser admin = new AppUser();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setEnabled(true);
        admin.setRoles(new LinkedHashSet<>(java.util.List.of(userRole, adminRole)));
        userRepository.save(admin);
    }

    private Role ensureRole(String name) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role role = new Role();
            role.setName(name);
            return roleRepository.save(role);
        });
    }
}
