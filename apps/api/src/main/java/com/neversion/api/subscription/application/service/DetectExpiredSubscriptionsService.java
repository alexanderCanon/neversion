package com.neversion.api.subscription.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.subscription.application.port.in.DetectExpiredSubscriptionsUseCase;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.subscription.domain.service.InventoryStateDomainService;
import com.neversion.api.subscription.domain.service.InventoryStateDomainService.InventoryMutation;

/**
 * US-047: Detects expired subscriptions and suspends access.
 */
@Service
public class DetectExpiredSubscriptionsService implements DetectExpiredSubscriptionsUseCase {

    private static final Logger log = LoggerFactory.getLogger(DetectExpiredSubscriptionsService.class);

    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final ProfileRepositoryPort profileRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final NotificationLogPort notificationLogPort;
    private final InventoryStateDomainService inventoryStateDomainService;
    private final Clock clock;

    public DetectExpiredSubscriptionsService(
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            ProfileRepositoryPort profileRepositoryPort,
            AccountRepositoryPort accountRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            NotificationLogPort notificationLogPort,
            InventoryStateDomainService inventoryStateDomainService,
            Clock clock) {
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.profileRepositoryPort = profileRepositoryPort;
        this.accountRepositoryPort = accountRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.notificationLogPort = notificationLogPort;
        this.inventoryStateDomainService = inventoryStateDomainService;
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

            // US-055: Notify the client that their subscription has expired
            notifyClientExpired(saved);

            suspendedByVendor.computeIfAbsent(saved.getVendorId(), ignored -> new ArrayList<>()).add(saved);
        }

        suspendedByVendor.forEach(this::recordVendorSummary);
        return expiredSubscriptions.size();
    }

    private void expireInventory(Account account, Profile selectedProfile) {
        List<Profile> accountProfiles = profileRepositoryPort.findByAccountId(account.getId());
        InventoryMutation mutation = inventoryStateDomainService.expire(
                account, selectedProfile, accountProfiles);
        profileRepositoryPort.saveAll(mutation.profilesToPersist());
        mutation.account().ifPresent(accountRepositoryPort::save);
    }

    /**
     * US-055: Sends SUBSCRIPTION_EXPIRED notification to the client.
     * Errors are logged but do not block the main flow.
     */
    private void notifyClientExpired(Subscription subscription) {
        try {
            clientRepositoryPort.findByInternalId(subscription.getClientId())
                    .ifPresent(client -> {
                        String payload = String.format(
                                "{\"subscriptionId\":\"%s\",\"clientName\":\"%s\"}",
                                subscription.getUuid(), client.getName());
                        notificationLogPort.record("SUBSCRIPTION_EXPIRED", client.getEmail(), payload,
                                "subscription", subscription.getId(), "due");
                    });
        } catch (Exception e) {
            log.warn("Failed to record client expiry notification for subscription {}: {}",
                    subscription.getUuid(), e.getMessage());
        }
    }

    private void recordVendorSummary(Long vendorId, List<Subscription> subscriptions) {
        String subscriptionIds = subscriptions.stream()
                .map(subscription -> "\"" + subscription.getUuid() + "\"")
                .reduce((left, right) -> left + "," + right)
                .orElse("");
        String payload = String.format(
                "{\"vendorId\":%d,\"expiredCount\":%d,\"subscriptionIds\":[%s]}",
                vendorId, subscriptions.size(), subscriptionIds);
        notificationLogPort.record("SUBSCRIPTIONS_EXPIRED_DAILY", "vendor:" + vendorId, payload,
                "vendor", vendorId, "expired_daily");
    }
}
