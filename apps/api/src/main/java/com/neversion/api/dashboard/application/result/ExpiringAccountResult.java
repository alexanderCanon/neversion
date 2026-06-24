package com.neversion.api.dashboard.application.result;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Row-level KPI projection for master accounts that require renewal follow-up.
 */
public record ExpiringAccountResult(
        UUID accountId,
        String serviceName,
        String accountEmail,
        LocalDate renewalDate,
        String status) {
}
