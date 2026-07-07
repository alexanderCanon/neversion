package com.neversion.api.game.infrastructure.adapters.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record GameRequest(

        @NotBlank(message = "Game name is required")
        @Schema(description = "Game name, e.g. Free Fire", example = "Free Fire")
        String name,

        @NotBlank(message = "Slug is required")
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                message = "Slug must be lowercase, alphanumeric, hyphen-separated")
        @Schema(description = "URL-friendly slug, unique per vendor", example = "free-fire")
        String slug,

        @Schema(description = "Optional URL to the game logo/image")
        String imageUrl
) {
}
