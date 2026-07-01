package com.neversion.api.vendor.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request body for updating a vendor's loyalty points (rewards) configuration.
 * <p>
 * The {@code rewardsCfg} field must be a valid JSON string:
 * <pre>{@json
 * { "enabled": true, "earn_pct": 2.0 }
 * }</pre>
 */
public record UpdateRewardsConfigRequest(

        @NotBlank(message = "rewardsCfg is required")
        @Schema(description = "Rewards/loyalty points configuration as a JSON string",
                example = "{\"enabled\":true,\"earn_pct\":2.0}")
        String rewardsCfg
) {
}
