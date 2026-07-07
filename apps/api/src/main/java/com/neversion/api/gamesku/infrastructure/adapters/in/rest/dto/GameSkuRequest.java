package com.neversion.api.gamesku.infrastructure.adapters.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record GameSkuRequest(

        @NotBlank(message = "Game SKU code is required")
        @Schema(description = "SKU identifier code, e.g. ff-110", example = "ff-110")
        String code,

        @NotBlank(message = "Game SKU name is required")
        @Schema(description = "SKU name, e.g. Free Fire 110 Diamonds", example = "Free Fire 110 Diamonds")
        String name,

        @NotNull(message = "Price is required")
        @PositiveOrZero(message = "Price must be positive or zero")
        @Schema(description = "Price of the SKU", example = "10.00")
        BigDecimal price,

        @Schema(description = "Optional URL to the SKU image")
        String imageUrl,

        @Schema(description = "UUID of the parent game this SKU belongs to")
        UUID gameUuid
) {
}
