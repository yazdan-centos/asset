package org.mapnaom.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class ReferenceDtos {

    private ReferenceDtos() {
    }

    public record NamedRequest(
            @NotBlank @Size(max = 32) String code,
            @NotBlank @Size(max = 150) String name,
            @NotNull Boolean active
    ) {
    }

    public record LocationRequest(
            @NotBlank @Size(max = 32) String code,
            @NotBlank @Size(max = 150) String name,
            @Size(max = 255) String address,
            @NotNull Boolean active
    ) {
    }

    public record PersonRequest(
            @NotBlank @Size(max = 32) String personnelCode,
            @NotBlank @Size(max = 150) String fullName,
            @NotNull Boolean active
    ) {
    }

    public record NamedResponse(
            Long id, Long version, Instant createdAt, Instant updatedAt,
            String code, String name, boolean active
    ) {
    }

    public record LocationResponse(
            Long id, Long version, Instant createdAt, Instant updatedAt,
            String code, String name, String address, boolean active
    ) {
    }

    public record PersonResponse(
            Long id, Long version, Instant createdAt, Instant updatedAt,
            String personnelCode, String fullName, boolean active
    ) {
    }
}
