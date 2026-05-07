package com.neversion.api.dashboard.application.result;

import java.util.List;

/**
 * US-063 KPI grouped by operational urgency.
 */
public record ExpiringSubscriptionsKpiResult(
        List<ExpiringSubscriptionResult> today,
        List<ExpiringSubscriptionResult> tomorrow,
        List<ExpiringSubscriptionResult> thisWeek) {
}
