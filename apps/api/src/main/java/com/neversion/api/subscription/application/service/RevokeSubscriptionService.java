package com.neversion.api.subscription.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.port.out.ProfileAssignmentHistoryRepositoryPort;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.application.service.VendorSecurityService;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.subscription.application.port.in.RevokeSubscriptionUseCase;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.subscription.domain.service.InventoryStateDomainService;
import com.neversion.api.subscription.domain.service.InventoryStateDomainService.InventoryMutation;

/**
 * US-046: Revokes subscription access and releases inventory.
 *
 * Additionally:
 * - Closes the open ProfileAssignmentHistory record (sets releasedAt).
 * - Resets the profile slot to a clean state for Spotify BY_PROFILE
 *   subscriptions (name → "Perfil", pin/notes → null).
 */
@Service
public class RevokeSubscriptionService implements RevokeSubscriptionUseCase {

    private static final Logger log = LoggerFactory.getLogger(RevokeSubscriptionService.class);

    private static final String SPOTIFY_SERVICE_NAME = "Spotify";

    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final ProfileRepositoryPort profileRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;
    private final NotificationLogPort notificationLogPort;
    private final InventoryStateDomainService inventoryStateDomainService;
    private final VendorSecurityService vendorSecurityService;
    private final ProfileAssignmentHistoryRepositoryPort historyRepositoryPort;

    public RevokeSubscriptionService(
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            ProfileRepositoryPort profileRepositoryPort,
            AccountRepositoryPort accountRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort,
            NotificationLogPort notificationLogPort,
            InventoryStateDomainService inventoryStateDomainService,
            VendorSecurityService vendorSecurityService,
            ProfileAssignmentHistoryRepositoryPort historyRepositoryPort) {
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.profileRepositoryPort = profileRepositoryPort;
        this.accountRepositoryPort = accountRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.notificationLogPort = notificationLogPort;
        this.inventoryStateDomainService = inventoryStateDomainService;
        this.vendorSecurityService = vendorSecurityService;
        this.historyRepositoryPort = historyRepositoryPort;
    }

    @Override
    @Transactional
    public Subscription revoke(UUID subscriptionUuid, String callerExternalId) {
        Subscription subscription = subscriptionRepositoryPort.findById(subscriptionUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found with id: " + subscriptionUuid));

        Long callerVendorId = vendorSecurityService.resolveVendorId(callerExternalId);
        vendorSecurityService.assertOwnership(callerVendorId, subscription.getVendorId(),
                "subscription " + subscriptionUuid);

        if (subscription.getStatus() == SubStatus.CANCELLED) {
            throw new BusinessRuleException("Subscription is already cancelled.");
        }

        Profile profile = profileRepositoryPort.findByInternalId(subscription.getProfileId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile not found for subscription: " + subscriptionUuid));
        Account account = accountRepositoryPort.findByInternalId(profile.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found for profile: " + profile.getUuid()));
        Client client = clientRepositoryPort.findByInternalId(subscription.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found for subscription: " + subscriptionUuid));

        // Close open assignment history record.
        closeAssignmentHistory(profile.getId());

        subscription.setStatus(SubStatus.CANCELLED);
        releaseInventory(account, profile);

        // Reset profile slot for Spotify BY_PROFILE after inventory has been released.
        if (account.getSaleMode() == SaleMode.BY_PROFILE) {
            resetSpotifyProfileSlot(profile, account);
        }

        Subscription saved = subscriptionRepositoryPort.save(subscription);
        recordRevocationNotification(saved, client);
        return saved;
    }

    // ── History management ──────────────────────────────────────────────────

    private void closeAssignmentHistory(Long profileId) {
        historyRepositoryPort.findOpenByProfileId(profileId).ifPresentOrElse(
                history -> {
                    history.setReleasedAt(LocalDateTime.now());
                    historyRepositoryPort.save(history);
                },
                () -> log.warn("No open assignment history found for profile id={}", profileId));
    }

    // ── Spotify slot reset ──────────────────────────────────────────────────

    /**
     * Resets the profile slot to a neutral state so it can be re-assigned to a
     * new client without leaking previous credentials.
     *
     * Only applies to Spotify BY_PROFILE (each slot maps to an independent
     * personal account / invitation link).
     */
    private void resetSpotifyProfileSlot(Profile profile, Account account) {
        com.neversion.api.service.domain.model.Service service;
        try {
            service = serviceRepositoryPort.findByInternalId(account.getServiceId())
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Could not load service for account {}; skipping slot reset.", account.getUuid(), e);
            return;
        }

        if (service == null || !SPOTIFY_SERVICE_NAME.equalsIgnoreCase(service.getName())) {
            return;
        }

        profile.setName("Perfil");
        profile.setPin(null);
        profile.setNotes(null);
        profileRepositoryPort.save(profile);
        log.info("Spotify profile slot {} reset to neutral state after subscription cancellation.",
                profile.getUuid());
    }

    // ── Inventory release ───────────────────────────────────────────────────

    private void releaseInventory(Account account, Profile selectedProfile) {
        List<Profile> accountProfiles = profileRepositoryPort.findByAccountId(account.getId());
        InventoryMutation mutation = inventoryStateDomainService.release(
                account, selectedProfile, accountProfiles);
        profileRepositoryPort.saveAll(mutation.profilesToPersist());
        mutation.account().ifPresent(accountRepositoryPort::save);
    }

    private void recordRevocationNotification(Subscription subscription, Client client) {
        String payload = String.format(
                "{\"subscriptionId\":\"%s\",\"clientId\":\"%s\"}",
                subscription.getUuid(), client.getUuid());
        notificationLogPort.record("ACCESS_REVOKED", client.getEmail(), payload,
                "subscription", subscription.getId(), "revoked");
    }
}
