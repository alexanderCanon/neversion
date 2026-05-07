package com.neversion.api.dashboard.application.result;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Row-level KPI projection for subscriptions that require renewal follow-up.
 */
public record ExpiringSubscriptionResult(
        UUID subscriptionId,
        String clientName,
        String serviceName,
        String profileName,
        LocalDate paymentDueDate,
        String status) {
}
