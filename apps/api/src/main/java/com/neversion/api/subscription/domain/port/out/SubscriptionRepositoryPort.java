package com.neversion.api.subscription.domain.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.SubscriptionListView;
import com.neversion.api.subscription.domain.model.enums.SubStatus;

public interface SubscriptionRepositoryPort {

    Subscription save(Subscription subscription);

    Optional<Subscription> findById(UUID uuid);

    Optional<Subscription> findByInternalId(Long id);

    Optional<Subscription> findByOrderId(Long orderId);

    List<Subscription> findByClientId(Long clientId);

    List<Subscription> findByProfileId(Long profileId);

    List<Subscription> findByStatus(SubStatus status);

    /**
     * US-043: Returns subscriptions scoped to a vendor, optionally filtered by
     * service and subscription status. Results are ordered by payment due date ASC.
     */
    List<Subscription> findByVendorIdFiltered(Long vendorId, Long serviceId, SubStatus status);

    /**
     * US-043 / tech-debt A3: Returns enriched list views for a vendor's
     * subscriptions in a single query (profile, client, account and service
     * joined), avoiding the per-row N+1 lookups previously done in the
     * controller. Ordered by payment due date ASC.
     */
    List<SubscriptionListView> findVendorSubscriptionViews(Long vendorId, Long serviceId, SubStatus status);

    List<Subscription> findAll();

    /**
     * Overbooking guard: a profile can only have one active subscription at a time (BR-04).
     * Returns true if an active subscription already exists for the given profile.
     */
    boolean existsActiveByProfileId(Long profileId);

    /**
     * Returns subscriptions whose payment_due_date is on or before the given date.
     * Used by background automations to detect overdue payments (BR-10).
     */
    List<Subscription> findOverdue(LocalDate asOf);

    /**
     * US-054: Finds active subscriptions due on a specific date.
     * Used by renewal reminder cron to detect 7d/3d/1d upcoming renewals.
     */
    List<Subscription> findActiveByPaymentDueDate(LocalDate dueDate);

    void deleteById(UUID uuid);
}
