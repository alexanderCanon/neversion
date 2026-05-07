package com.neversion.api.service.infrastructure.adapters.in.rest.dto;

import com.neversion.api.shared.domain.model.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response body for digital service operations (US-017 / US-018 / US-019 / US-020 / US-021).
 * Exposes all relevant fields for both vendor panel and public store views.
 */
@Builder
public record ServiceResponse(

        @Schema(description = "Public UUID of the service")
        UUID id,

        @Schema(description = "Service name")
        String name,

        @Schema(description = "Service category")
        CategoryType category,

        @Schema(description = "Service description")
        String description,

        @Schema(description = "URL to the service logo/image")
        String imageUrl,

        @Schema(description = "Price per individual profile sale")
        BigDecimal priceProfile,

        @Schema(description = "Price for full account sale (Cuenta Completa)")
        BigDecimal priceComplete,

        @Schema(description = "Subscription duration in days")
        Integer durationDays,

        @Schema(description = "Maximum profiles allowed per account")
        Integer maxProfiles,

        @Schema(description = "Whether the service is active and visible in the store")
        Boolean isActive,

        @Schema(description = "Free-form JSON metadata")
        String details,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt
) {
}
