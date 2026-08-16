package org.mapnaom.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class RoleDtos {

    private RoleDtos() {
    }

    public record Request(@NotBlank @Size(max = 64) String name) {
    }

    public record Response(
            Long id,
            Long version,
            Instant createdAt,
            Instant updatedAt,
            String name
    ) {
    }
}
