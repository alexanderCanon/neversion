package com.neversion.api.account.infrastructure.adapters.in.rest.dto;

import java.util.List;

import lombok.Builder;

/**
 * Full account detail response (US-028).
 * Includes master account data, all profiles, and status counters.
 */
@Builder
public record AccountDetailResponse(
        AccountResponse account,
        List<ProfileSummaryResponse> profiles,
        AccountSummaryResponse summary) {
}
