package com.neversion.api.account.infrastructure.adapters.in.rest.dto;

import lombok.Builder;

/**
 * Counters summary for the account detail view (US-028).
 */
@Builder
public record AccountSummaryResponse(
        int total,
        int available,
        int active,
        int reserved,
        int occupied,
        int blocked,
        int expired) {
}
