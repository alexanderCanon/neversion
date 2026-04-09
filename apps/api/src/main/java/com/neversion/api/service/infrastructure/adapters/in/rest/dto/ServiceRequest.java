package com.neversion.api.service.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record ServiceRequest(
        @NotBlank(message = "Service name is required") String name,

        @Positive(message = "Max profiles must be positive") Integer maxProfiles,

        /** JSONB — free-form metadata. Example: {"platform":"Netflix","category":"streaming"} */
        String details) {
}
