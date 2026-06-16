package com.neversion.api.client.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request payload for client self-service profile updates (EPIC-09 / US-062).
 * Email is intentionally excluded because it is tied to external authentication.
 */
public record UpdateClientProfileRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Phone is required")
        @Size(max = 50, message = "Phone must not exceed 50 characters")
        @Pattern(
                regexp = "^(?:\\+?502[-\\s]?)?[23457]\\d{3}[-\\s]?\\d{4}$",
                message = "El número de teléfono debe ser de Guatemala (8 dígitos locales comenzando con 2, 3, 4, 5 o 7)."
        )
        String phone) {
}
