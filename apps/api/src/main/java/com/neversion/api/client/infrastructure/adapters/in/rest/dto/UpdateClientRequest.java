package com.neversion.api.client.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Request payload for updating basic client data (US-032).
 * email is intentionally excluded — it is immutable after creation (BR-US032-01).
 */
@Builder
public record UpdateClientRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @Size(max = 50, message = "Phone must not exceed 50 characters")
        String phone,

        String notes) {
}
