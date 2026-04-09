package com.neversion.api.dashboard.application.result;

import java.util.UUID;

/**
 * Read-only projection for endpoint 3: profile with optional subscription.
 */
public record ProfileResult(
        UUID profileId,
        String profileName,
        String pin,
        String profileStatus,
        ProfileSubscriptionResult subscription) {
}
