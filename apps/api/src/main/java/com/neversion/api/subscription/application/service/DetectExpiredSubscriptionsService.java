package com.neversion.api.subscription.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.shared.domain.model.enums.AccountStatus;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.subscription.application.port.in.DetectExpiredSubscriptionsUseCase;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;

/**
 * US-047: Detects expired subscriptions and suspends access.
 */
@Service
public class DetectExpiredSubscriptionsService implements DetectExpiredSubscriptionsUseCase {

    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final ProfileRepositoryPort profileRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final NotificationLogPort notificationLogPort;
    private final Clock clock;

    public DetectExpiredSubscriptionsService(
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            ProfileRepositoryPort profileRepositoryPort,
            AccountRepositoryPort accountRepositoryPort,
            NotificationLogPort notificationLogPort,
            Clock clock) {
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.profileRepositoryPort = profileRepositoryPort;
        this.accountRepositoryPort = accountRepositoryPort;
        this.notificationLogPort = notificationLogPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public int detectAndSuspend() {
        List<Subscription> expiredSubscriptions = subscriptionRepositoryPort.findOverdue(LocalDate.now(clock));
        Map<Long, List<Subscription>> suspendedByVendor = new LinkedHashMap<>();

        for (Subscription subscription : expiredSubscriptions) {
            Profile profile = profileRepositoryPort.findByInternalId(subscription.getProfileId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Profile not found for subscription: " + subscription.getUuid()));
            Account account = accountRepositoryPort.findByInternalId(profile.getAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Account not found for profile: " + profile.getUuid()));

            subscription.setStatus(SubStatus.SUSPENDED);
            expireInventory(account, profile);
            Subscription saved = subscriptionRepositoryPort.save(subscription);

            suspendedByVendor.computeIfAbsent(saved.getVendorId(), ignored -> new ArrayList<>()).add(saved);
        }

        suspendedByVendor.forEach(this::recordVendorSummary);
        return expiredSubscriptions.size();
    }

    private void expireInventory(Account account, Profile selectedProfile) {
        if (account.getSaleMode() == SaleMode.FULL_ACCOUNT) {
            var profiles = profileRepositoryPort.findByAccountId(account.getId());
            profiles.forEach(profile -> profile.setStatus(ProfileStatus.EXPIRED));
            profileRepositoryPort.saveAll(profiles);
            account.setStatus(AccountStatus.EXPIRED);
            accountRepositoryPort.save(account);
            return;
        }

        selectedProfile.setStatus(ProfileStatus.EXPIRED);
        profileRepositoryPort.save(selectedProfile);
    }

    private void recordVendorSummary(Long vendorId, List<Subscription> subscriptions) {
        String subscriptionIds = subscriptions.stream()
                .map(subscription -> "\"" + subscription.getUuid() + "\"")
                .reduce((left, right) -> left + "," + right)
                .orElse("");
        String payload = String.format(
                "{\"vendorId\":%d,\"expiredCount\":%d,\"subscriptionIds\":[%s]}",
                vendorId, subscriptions.size(), subscriptionIds);
        notificationLogPort.record("SUBSCRIPTIONS_EXPIRED_DAILY", "vendor:" + vendorId, payload);
    }
}
