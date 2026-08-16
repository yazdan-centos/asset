package org.mapnaom.asset.service;

import lombok.RequiredArgsConstructor;
import org.mapnaom.asset.dto.RoleDtos.Request;
import org.mapnaom.asset.dto.RoleDtos.Response;
import org.mapnaom.asset.entity.Role;
import org.mapnaom.asset.exception.ResourceNotFoundException;
import org.mapnaom.asset.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roleRepository;

    public List<Response> findAll() {
        return roleRepository.findAll().stream().map(this::toResponse).toList();
    }

    public Response findById(Long id) {
        return toResponse(require(id));
    }

    @Transactional
    public Response create(Request request) {
        String name = normalize(request.name());
        if (roleRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Role already exists: " + name);
        }
        Role role = new Role();
        role.setName(name);
        return toResponse(roleRepository.save(role));
    }

    @Transactional
    public Response update(Long id, Request request) {
        Role role = require(id);
        String name = normalize(request.name());
        roleRepository.findByName(name)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Role already exists: " + name);
                });
        role.setName(name);
        return toResponse(roleRepository.save(role));
    }

    @Transactional
    public void delete(Long id) {
        roleRepository.delete(require(id));
        roleRepository.flush();
    }

    public String normalize(String name) {
        String normalized = name.trim().toUpperCase();
        return normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized;
    }

    private Role require(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + id));
    }

    private Response toResponse(Role role) {
        return new Response(role.getId(), role.getVersion(), role.getCreatedAt(), role.getUpdatedAt(), role.getName());
    }
}
