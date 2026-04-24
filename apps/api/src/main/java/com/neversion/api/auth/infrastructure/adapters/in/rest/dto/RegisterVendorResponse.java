package com.neversion.api.auth.infrastructure.adapters.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Response body returned after a successful vendor registration.
 * <p>
 * Only public UUIDs are exposed — internal BIGINT IDs are never returned (NFR-01).
 * The temporaryPassword is included so the Super Admin can manually create
 * the Supabase Auth account (ADR-09 — manual step).
 */
public record RegisterVendorResponse(

        @Schema(description = "Public UUID of the created platform user")
        UUID userUuid,

        @Schema(description = "Public UUID of the created vendor record")
        UUID vendorUuid,

        @Schema(description = "Vendor's store display name")
        String storeName,

        @Schema(description = "Vendor's email address")
        String email,

        @Schema(description = "System-generated temporary password. " +
                "Use this to create the Supabase Auth account manually.")
        String temporaryPassword,

        @Schema(description = "Reminder: create the Supabase Auth account manually " +
                "using the email and temporaryPassword above.")
        String manualStep
) {
}
