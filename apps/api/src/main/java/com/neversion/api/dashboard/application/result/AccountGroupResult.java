package com.neversion.api.dashboard.application.result;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Read-only projection for endpoint 2: account with profile availability.
 */
public record AccountGroupResult(
        UUID accountId,
        String email,
        String password,
        LocalDate cutOffDate,
        String accountType,
        String accountStatus,
        int maxProfiles,
        int occupiedProfiles,
        int availableProfiles,
        String availability) {
}
