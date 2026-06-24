package com.neversion.api.dashboard.application.result;

import java.util.List;

/**
 * KPI for master account renewals grouped by operational urgency.
 */
public record ExpiringAccountsKpiResult(
        List<ExpiringAccountResult> today,
        List<ExpiringAccountResult> tomorrow,
        List<ExpiringAccountResult> thisWeek) {
}
