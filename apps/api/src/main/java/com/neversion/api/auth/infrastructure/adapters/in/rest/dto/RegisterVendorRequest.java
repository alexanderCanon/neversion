package com.neversion.api.auth.infrastructure.adapters.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for vendor registration (US-012).
 * Only the Super Admin can call this endpoint.
 * <p>
 * The backend manages Supabase Auth account creation directly (Backend-Driven Auth).
 */
public record RegisterVendorRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        @Schema(description = "Vendor's email address", example = "tienda@ejemplo.com")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        @Schema(description = "Vendor's chosen password", example = "Secret123!")
        String password,

        @NotBlank(message = "Store name is required")
        @Size(max = 150, message = "Store name must not exceed 150 characters")
        @Schema(description = "Display name for the vendor's storefront", example = "Mi Tienda Digital")
        String storeName,

        @Schema(description = "Optional URL to the vendor's logo", example = "https://cdn.example.com/logo.png")
        String logoUrl,

        @Schema(description = "Optional JSON with bank/payment details",
                example = "{\"bank\":\"Banrural\",\"account\":\"123456\"}")
        String bankDetails,

        @Schema(description = "Optional JSON with discount tier configuration (BR-13)",
                example = "{\"min_items\":2,\"tiers\":[{\"from\":2,\"to\":3,\"discount_pct\":5}]}")
        String discountCfg
) {
}
