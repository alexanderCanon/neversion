package com.neversion.api.vendor.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request body for updating a vendor's discount configuration (BR-13 v2).
 * <p>
 * The {@code discountCfg} field must be a valid JSON string conforming to the
 * BR-13 structure:
 * <pre>{@json
 * {
 *   "min_items": 2,
 *   "max_items": 4,
 *   "round_to": 5,
 *   "tiers": [
 *     { "count": 2, "discount_pct": 25 },
 *     { "count": 3, "discount_pct": 18 },
 *     { "count": 4, "discount_pct": 22 }
 *   ]
 * }
 * }</pre>
 */
public record UpdateDiscountConfigRequest(

        @NotBlank(message = "discountCfg is required")
        @Schema(description = "Discount tier configuration as a JSON string (BR-13)",
                example = "{\"min_items\":2,\"max_items\":4,\"round_to\":5,\"tiers\":[{\"count\":2,\"discount_pct\":25},{\"count\":3,\"discount_pct\":18},{\"count\":4,\"discount_pct\":22}]}")
        String discountCfg
) {
}
