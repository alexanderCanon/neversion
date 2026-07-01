package com.neversion.api.loyalty.infrastructure.adapters.in.rest.dto;

import java.util.List;

/**
 * Paginated response wrapper for points ledger movements.
 */
public record PointsMovementsPageResponse(
        List<PointsMovementResponse> movements,
        long totalElements) {
}
