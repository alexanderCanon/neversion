package com.neversion.api.loyalty.infrastructure.adapters.in.rest.dto;

/**
 * Response body for a client's points balance summary.
 */
public record PointsSummaryResponse(long available, long pending, long total) {
}
