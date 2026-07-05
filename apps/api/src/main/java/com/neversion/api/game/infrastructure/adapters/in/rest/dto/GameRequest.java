package com.neversion.api.game.infrastructure.adapters.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record GameRequest(

        @NotBlank(message = "Game code is required")
        @Schema(description = "Game identifier code, e.g. ff-100", example = "ff-100")
        String code,

        @NotBlank(message = "Game name is required")
        @Schema(description = "Game name, e.g. Free Fire 100 Diamonds", example = "Free Fire 100 Diamonds")
        String name,

        @NotNull(message = "Price is required")
        @PositiveOrZero(message = "Price must be positive or zero")
        @Schema(description = "Price of the game item", example = "10.00")
        BigDecimal price,

        @Schema(description = "Optional URL to the game logo/image")
        String imageUrl
) {
}
