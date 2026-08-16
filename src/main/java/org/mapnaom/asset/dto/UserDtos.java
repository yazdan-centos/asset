package org.mapnaom.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Set;

public final class UserDtos {

    private UserDtos() {
    }

    public record CreateRequest(
            @NotBlank @Size(max = 100) String username,
            @NotBlank @Size(min = 8, max = 100) String password,
            @NotNull Boolean enabled,
            @NotEmpty Set<String> roles
    ) {
    }

    public record UpdateRequest(
            @NotBlank @Size(max = 100) String username,
            @Size(min = 8, max = 100) String password,
            @NotNull Boolean enabled,
            @NotEmpty Set<String> roles
    ) {
    }

    public record Response(
            Long id,
            Long version,
            Instant createdAt,
            Instant updatedAt,
            String username,
            boolean enabled,
            Set<String> roles
    ) {
    }
}
