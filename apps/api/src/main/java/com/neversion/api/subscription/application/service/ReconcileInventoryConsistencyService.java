package com.neversion.api.subscription.application.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.subscription.application.port.in.ReconcileInventoryConsistencyUseCase;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;

/**
 * Tech-debt remediation A1 (Phase 1 — safety net).
 * <p>
 * Read-only reconciliation that flags subscriptions whose assigned profile is
 * in an unexpected state. It does NOT correct anything — it only logs, so that
 * silent inventory drift becomes visible without risking automatic data
 * changes.
 * <p>
 * Expected profile state per subscription status:
 * <ul>
 *   <li>{@code ACTIVE}    → profile must be {@code ACTIVE}</li>
 *   <li>{@code SUSPENDED} → profile must be {@code RESERVED} (manual suspend)
 *       or {@code EXPIRED} (automatic expiry detection)</li>
 * </ul>
 */
@Service
public class ReconcileInventoryConsistencyService implements ReconcileInventoryConsistencyUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReconcileInventoryConsistencyService.class);

    private static final Set<ProfileStatus> EXPECTED_FOR_ACTIVE =
            EnumSet.of(ProfileStatus.ACTIVE);
    private static final Set<ProfileStatus> EXPECTED_FOR_SUSPENDED =
            EnumSet.of(ProfileStatus.RESERVED, ProfileStatus.EXPIRED);

    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final ProfileRepositoryPort profileRepositoryPort;

    public ReconcileInventoryConsistencyService(
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            ProfileRepositoryPort profileRepositoryPort) {
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.profileRepositoryPort = profileRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public int detectInconsistencies() {
        int inconsistencies = 0;
        inconsistencies += scan(SubStatus.ACTIVE, EXPECTED_FOR_ACTIVE);
        inconsistencies += scan(SubStatus.SUSPENDED, EXPECTED_FOR_SUSPENDED);

        if (inconsistencies > 0) {
            log.warn("Inventory reconciliation found {} inconsistency(ies). "
                    + "Review the WARN entries above; no automatic correction was applied.",
                    inconsistencies);
        }
        return inconsistencies;
    }

    private int scan(SubStatus subscriptionStatus, Set<ProfileStatus> expectedProfileStatuses) {
        List<Subscription> subscriptions = subscriptionRepositoryPort.findByStatus(subscriptionStatus);
        int count = 0;

        for (Subscription subscription : subscriptions) {
            Profile profile = profileRepositoryPort.findByInternalId(subscription.getProfileId())
                    .orElse(null);

            if (profile == null) {
                count++;
                log.warn("Inventory inconsistency: subscription {} (status={}) references "
                        + "profileId={} which does not exist.",
                        subscription.getUuid(), subscriptionStatus, subscription.getProfileId());
                continue;
            }

            if (!expectedProfileStatuses.contains(profile.getStatus())) {
                count++;
                log.warn("Inventory inconsistency: subscription {} (status={}) has profile {} "
                        + "in status {}; expected one of {}.",
                        subscription.getUuid(), subscriptionStatus, profile.getUuid(),
                        profile.getStatus(), expectedProfileStatuses);
            }
        }
        return count;
    }
}
