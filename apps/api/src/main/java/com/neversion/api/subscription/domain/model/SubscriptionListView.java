package com.neversion.api.subscription.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.neversion.api.subscription.domain.model.enums.SubStatus;

/**
 * Read-only projection for the vendor subscription list (US-043).
 * <p>
 * Tech-debt remediation A3: replaces the per-row N+1 enrichment previously done
 * in the controller. Populated by a single JOIN query that pulls the profile,
 * client, account and service data the list view needs.
 */
public record SubscriptionListView(
        UUID subscriptionUuid,
        UUID profileUuid,
        String profileName,
        UUID clientUuid,
        String clientName,
        UUID accountUuid,
        String serviceName,
        SubStatus status,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate paymentDueDate,
        Long monthsPaid,
        String notes,
        LocalDateTime createdAt) {
}
