package com.neversion.api.subscription.domain.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;

public interface SubscriptionRepositoryPort {

    Subscription save(Subscription subscription);

    Optional<Subscription> findById(UUID uuid);

    Optional<Subscription> findByInternalId(Long id);

    List<Subscription> findByClientId(Long clientId);

    List<Subscription> findByProfileId(Long profileId);

    List<Subscription> findByStatus(SubStatus status);

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

    void deleteById(UUID uuid);
}
