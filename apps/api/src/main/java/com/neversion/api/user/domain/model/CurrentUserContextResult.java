package com.neversion.api.user.domain.model;

import com.neversion.api.user.domain.model.enums.UserRole;

import java.util.UUID;

/**
 * Authenticated platform context resolved from the Supabase JWT subject.
 * Exposes only public UUIDs to REST adapters.
 */
public record CurrentUserContextResult(
        String externalId,
        UserRole role,
        UUID vendorUuid,
        String storeName
) {
}

