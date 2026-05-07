package com.neversion.api.auth.infrastructure.adapters.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Response body returned after a successful client registration (US-013).
 * <p>
 * Only public UUIDs are exposed — internal BIGINT IDs are never returned (NFR-01).
 */
public record RegisterClientResponse(

        @Schema(description = "Public UUID of the created platform user")
        UUID userUuid,

        @Schema(description = "Public UUID of the created client record")
        UUID clientUuid,

        @Schema(description = "Client's display name")
        String name,

        @Schema(description = "Client's email address")
        String email
) {
}
