package com.neversion.api.client.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Request payload for creating a client manually (US-031).
 * phone is required as the vendor-scoped operational identifier.
 * email is optional and becomes relevant when the client activates access.
 * email is immutable after creation (BR-US032-01).
 */
@Builder
public record ClientRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @Email(message = "Email must be a valid email address")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Phone is required")
        @Size(max = 50, message = "Phone must not exceed 50 characters")
        @Pattern(
                regexp = "^(?:\\+?502[-\\s]?)?[23457]\\d{3}[-\\s]?\\d{4}$",
                message = "El número de teléfono debe ser de Guatemala (8 dígitos locales comenzando con 2, 3, 4, 5 o 7)."
        )
        String phone,

        String notes) {
}
