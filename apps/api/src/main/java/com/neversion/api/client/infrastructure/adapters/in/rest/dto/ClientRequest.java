package com.neversion.api.client.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Request payload for creating a client manually (US-031).
 * email is required and validated as a proper email address.
 * email is immutable after creation (BR-US032-01).
 */
@Builder
public record ClientRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @Size(max = 50, message = "Phone must not exceed 50 characters")
        String phone,

        String notes) {
}
