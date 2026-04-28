package com.neversion.api.auth.infrastructure.adapters.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request body for client self-registration (US-013).
 * This endpoint is public — any visitor can register as a client
 * on a specific vendor's store.
 * <p>
 * The frontend must create the Supabase Auth account first (ADR-09 revised)
 * and provide the resulting Supabase UUID as {@code externalId}.
 */
public record RegisterClientRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        @Schema(description = "Client's email address", example = "cliente@correo.com")
        String email,

        @NotBlank(message = "Supabase external ID is required")
        @Schema(description = "Supabase Auth UUID assigned to this client after account creation",
                example = "a1b2c3d4-5678-4abc-8def-000000000002")
        String externalId,

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        @Schema(description = "Client's display name", example = "Juan Pérez")
        String name,

        @Schema(description = "Optional phone number — primary WhatsApp contact", example = "+502 5555-1234")
        String phone,

        @NotNull(message = "Vendor UUID is required")
        @Schema(description = "UUID of the vendor (store) to register with",
                example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        UUID vendorUuid
) {
}
