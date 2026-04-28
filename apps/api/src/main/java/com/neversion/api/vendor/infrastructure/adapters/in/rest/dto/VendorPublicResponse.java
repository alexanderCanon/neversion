package com.neversion.api.vendor.infrastructure.adapters.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Public-facing vendor information for the storefront (multitenancy resolution).
 * Only exposes data safe for public consumption — no bank details or internal IDs.
 */
public record VendorPublicResponse(

        @Schema(description = "Public UUID of the vendor — used for multi-tenant API calls")
        UUID id,

        @Schema(description = "Display name of the vendor's store", example = "Mi Tienda Digital")
        String storeName,

        @Schema(description = "URL to the vendor's logo image", example = "https://cdn.example.com/logo.png")
        String logoUrl
) {
}
