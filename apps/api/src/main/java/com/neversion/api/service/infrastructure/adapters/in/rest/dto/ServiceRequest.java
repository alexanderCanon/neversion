package com.neversion.api.service.infrastructure.adapters.in.rest.dto;

import com.neversion.api.shared.domain.model.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * Request body for creating or updating a digital service (US-017 / US-018).
 * All required fields per US-017 acceptance criteria are validated here.
 */
@Builder
public record ServiceRequest(

        @NotBlank(message = "Service name is required")
        @Schema(description = "Service name, e.g. Netflix Premium", example = "Netflix Premium")
        String name,

        @NotNull(message = "Category is required")
        @Schema(description = "Service category")
        CategoryType category,

        @NotNull(message = "Price per profile is required")
        @Positive(message = "priceProfile must be positive")
        @Schema(description = "Price per individual profile sale", example = "45.00")
        BigDecimal priceProfile,

        @NotNull(message = "Full account price is required")
        @Positive(message = "priceComplete must be positive")
        @Schema(description = "Price for a full account sale (Cuenta Completa)", example = "150.00")
        BigDecimal priceComplete,

        @NotNull(message = "Duration days is required")
        @Positive(message = "durationDays must be positive")
        @Schema(description = "Subscription duration in days", example = "30")
        Integer durationDays,

        @NotNull(message = "Max profiles is required")
        @Positive(message = "Max profiles must be positive")
        @Schema(description = "Maximum profiles allowed per account", example = "5")
        Integer maxProfiles,

        @Schema(description = "Optional service description")
        String description,

        @Schema(description = "Optional URL to the service logo/image")
        String imageUrl,

        /** JSONB — free-form metadata. Example: {"platform":"Netflix"} */
        @Schema(description = "Optional free-form JSON metadata")
        String details
) {
}
