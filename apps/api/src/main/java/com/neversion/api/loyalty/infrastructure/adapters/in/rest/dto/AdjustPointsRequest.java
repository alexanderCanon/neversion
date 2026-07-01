package com.neversion.api.loyalty.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for a manual vendor-initiated points adjustment (+/-).
 */
public record AdjustPointsRequest(
        @NotNull Long points,
        @NotBlank String notes) {
}
