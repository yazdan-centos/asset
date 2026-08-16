package org.mapnaom.asset.service;

import lombok.RequiredArgsConstructor;
import org.mapnaom.asset.dto.UserDtos.CreateRequest;
import org.mapnaom.asset.dto.UserDtos.Response;
import org.mapnaom.asset.dto.UserDtos.UpdateRequest;
import org.mapnaom.asset.entity.AppUser;
import org.mapnaom.asset.entity.Role;
import org.mapnaom.asset.exception.ResourceNotFoundException;
import org.mapnaom.asset.repository.AppUserRepository;
import org.mapnaom.asset.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public List<Response> findAll() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public Response findById(Long id) {
        return toResponse(require(id));
    }

    @Transactional
    public Response create(CreateRequest request) {
        String username = normalizeUsername(request.username());
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEnabled(request.enabled());
        user.setRoles(resolveRoles(request.roles()));
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public Response update(Long id, UpdateRequest request) {
        AppUser user = require(id);
        String username = normalizeUsername(request.username());
        userRepository.findByUsername(username)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Username already exists: " + username);
                });
        user.setUsername(username);
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        user.setEnabled(request.enabled());
        user.setRoles(resolveRoles(request.roles()));
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        userRepository.delete(require(id));
        userRepository.flush();
    }

    private AppUser require(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private Set<Role> resolveRoles(Set<String> requestedNames) {
        Set<String> names = requestedNames.stream().map(roleService::normalize)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<Role> roles = roleRepository.findAllByNameIn(names);
        Set<String> found = roles.stream().map(Role::getName).collect(Collectors.toSet());
        Set<String> missing = new LinkedHashSet<>(names);
        missing.removeAll(found);
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Unknown roles: " + String.join(", ", missing));
        }
        return new LinkedHashSet<>(roles);
    }

    private Response toResponse(AppUser user) {
        Set<String> roles = user.getRoles().stream().map(Role::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new Response(user.getId(), user.getVersion(), user.getCreatedAt(), user.getUpdatedAt(),
                user.getUsername(), user.isEnabled(), roles);
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase();
    }
}
