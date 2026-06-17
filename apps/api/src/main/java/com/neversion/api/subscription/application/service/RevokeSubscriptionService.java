package com.neversion.api.subscription.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
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
 */
@Service
public class RevokeSubscriptionService implements RevokeSubscriptionUseCase {

    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final ProfileRepositoryPort profileRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final NotificationLogPort notificationLogPort;
    private final InventoryStateDomainService inventoryStateDomainService;
    private final VendorSecurityService vendorSecurityService;

    public RevokeSubscriptionService(
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            ProfileRepositoryPort profileRepositoryPort,
            AccountRepositoryPort accountRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            NotificationLogPort notificationLogPort,
            InventoryStateDomainService inventoryStateDomainService,
            VendorSecurityService vendorSecurityService) {
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.profileRepositoryPort = profileRepositoryPort;
        this.accountRepositoryPort = accountRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.notificationLogPort = notificationLogPort;
        this.inventoryStateDomainService = inventoryStateDomainService;
        this.vendorSecurityService = vendorSecurityService;
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

        subscription.setStatus(SubStatus.CANCELLED);
        releaseInventory(account, profile);

        Subscription saved = subscriptionRepositoryPort.save(subscription);
        recordRevocationNotification(saved, client);
        return saved;
    }

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
