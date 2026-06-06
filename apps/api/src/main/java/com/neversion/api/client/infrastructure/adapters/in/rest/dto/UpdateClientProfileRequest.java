package com.neversion.api.client.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
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
        String phone) {
}
