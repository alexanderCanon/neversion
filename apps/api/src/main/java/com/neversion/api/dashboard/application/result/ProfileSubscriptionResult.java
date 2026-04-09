package com.neversion.api.dashboard.application.result;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Subscription data nested inside a profile result.
 */
public record ProfileSubscriptionResult(
        UUID subscriptionId,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        ProfileCustomerResult customer) {
}
