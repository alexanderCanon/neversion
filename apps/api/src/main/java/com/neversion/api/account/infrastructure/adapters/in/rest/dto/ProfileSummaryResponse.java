package com.neversion.api.account.infrastructure.adapters.in.rest.dto;

import java.util.UUID;

import com.neversion.api.profile.domain.model.enums.ProfileStatus;

import lombok.Builder;

/**
 * Profile entry in the account detail response (US-028).
 * Intentionally lightweight — only data visible in the operational panel.
 */
@Builder
public record ProfileSummaryResponse(
        UUID id,
        String name,
        String pin,
        String notes,
        Boolean isOwner,
        ProfileStatus status) {
}
