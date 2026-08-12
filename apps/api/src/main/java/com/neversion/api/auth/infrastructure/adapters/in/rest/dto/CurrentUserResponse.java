package com.neversion.api.auth.infrastructure.adapters.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record CurrentUserResponse(

        @Schema(description = "External Supabase subject linked to the platform user")
        String externalId,

        @Schema(description = "Platform role in lowercase")
        String role,

        @Schema(description = "Public UUID of the vendor record when the caller is a vendor")
        UUID vendorUuid,

        @Schema(description = "Vendor store display name when the caller is a vendor")
        String storeName
) {
}

