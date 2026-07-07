package com.neversion.api.game.infrastructure.adapters.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record GameResponse(

        @Schema(description = "Public UUID of the game")
        UUID id,

        @Schema(description = "Game name")
        String name,

        @Schema(description = "URL-friendly slug, unique per vendor")
        String slug,

        @Schema(description = "URL to the game logo/image")
        String imageUrl,

        @Schema(description = "Whether the game is active and visible in the store")
        Boolean isActive,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt
) {
}
