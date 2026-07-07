package com.neversion.api.gamesku.infrastructure.adapters.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record GameSkuResponse(

        @Schema(description = "Public UUID of the game SKU")
        UUID id,

        @Schema(description = "SKU identifier code")
        String code,

        @Schema(description = "SKU name")
        String name,

        @Schema(description = "Price of the SKU")
        BigDecimal price,

        @Schema(description = "URL to the SKU image")
        String imageUrl,

        @Schema(description = "Whether the SKU is active and visible in the store")
        Boolean isActive,

        @Schema(description = "UUID of the parent game")
        UUID gameUuid,

        @Schema(description = "Slug of the parent game (filled by service)")
        String gameSlug,

        @Schema(description = "Name of the parent game (filled by service)")
        String gameName,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt
) {
}
