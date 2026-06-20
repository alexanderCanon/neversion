package com.neversion.api.auth.infrastructure.adapters.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request body for client self-registration (US-013).
 * This endpoint is public — any visitor can register as a client
 * on a specific vendor's store.
 * <p>
 * The backend manages Supabase Auth account creation directly (Backend-Driven Auth).
 */
public record RegisterClientRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        @Schema(description = "Client's email address", example = "cliente@correo.com")
        String email,

        @Schema(description = "Client's chosen password (required for standard registration)", example = "Secret123!")
        String password,

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        @Schema(description = "Client's display name", example = "Juan Pérez")
        String name,

        @NotBlank(message = "Phone is required")
        @Schema(description = "Required phone number — used to link existing manual clients", example = "+502 5555-1234")
        @Pattern(
                regexp = "^(?:\\+?502[-\\s]?)?[23457]\\d{3}[-\\s]?\\d{4}$",
                message = "El número de teléfono debe ser de Guatemala (8 dígitos locales comenzando con 2, 3, 4, 5 o 7)."
        )
        String phone,

        @NotNull(message = "Vendor UUID is required")
        @Schema(description = "UUID of the vendor (store) to register with",
                example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        UUID vendorUuid,

        @Schema(description = "Optional external auth user ID from Supabase (required for OAuth)",
                example = "supabase-uuid-abc-123")
        String externalId
) {
}
