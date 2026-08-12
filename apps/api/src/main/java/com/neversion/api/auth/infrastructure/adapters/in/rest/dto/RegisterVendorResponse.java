package com.neversion.api.auth.infrastructure.adapters.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Response body returned after a successful vendor registration (US-012).
 * <p>
 * Only public UUIDs are exposed — internal BIGINT IDs are never returned (NFR-01).
 */
public record RegisterVendorResponse(

        @Schema(description = "External Supabase subject linked to the platform user")
        String externalId,

        @Schema(description = "Public UUID of the created vendor record")
        UUID vendorUuid,

        @Schema(description = "Vendor's store display name")
        String storeName,

        @Schema(description = "Vendor's email address")
        String email
) {
}

