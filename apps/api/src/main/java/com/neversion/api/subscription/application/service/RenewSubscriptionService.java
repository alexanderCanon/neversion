package com.neversion.api.subscription.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
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
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.shared.domain.model.enums.AccountStatus;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.subscription.application.port.in.RenewSubscriptionUseCase;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.subscription.domain.service.SubscriptionRenewalDomainService;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * US-045: Renews subscriptions using BR-07 and restores access state.
 */
@Service
public class RenewSubscriptionService implements RenewSubscriptionUseCase {

    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final ProfileRepositoryPort profileRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final NotificationLogPort notificationLogPort;
    private final SubscriptionRenewalDomainService renewalDomainService;
    private final Clock clock;
    private final int gracePeriodDays;

    public RenewSubscriptionService(
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            ProfileRepositoryPort profileRepositoryPort,
            AccountRepositoryPort accountRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            NotificationLogPort notificationLogPort,
            SubscriptionRenewalDomainService renewalDomainService,
            Clock clock,
            @Value("${neversion.renewal.grace-period-days}") int gracePeriodDays) {
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.profileRepositoryPort = profileRepositoryPort;
        this.accountRepositoryPort = accountRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.notificationLogPort = notificationLogPort;
        this.renewalDomainService = renewalDomainService;
        this.clock = clock;
        this.gracePeriodDays = gracePeriodDays;
    }

    @Override
    @Transactional
    public Subscription renew(UUID subscriptionUuid, String callerExternalId) {
        Subscription subscription = subscriptionRepositoryPort.findById(subscriptionUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found with id: " + subscriptionUuid));

        Long callerVendorId = resolveVendorId(callerExternalId);
        if (!Objects.equals(callerVendorId, subscription.getVendorId())) {
            throw new AccessDeniedException("Access denied: subscription " + subscriptionUuid
                    + " does not belong to your vendor");
        }

        if (subscription.getStatus() != SubStatus.ACTIVE
                && subscription.getStatus() != SubStatus.SUSPENDED) {
            throw new BusinessRuleException("Only ACTIVE or SUSPENDED subscriptions can be renewed.");
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

        LocalDate paymentDate = LocalDate.now(clock);
        LocalDate newDueDate = renewalDomainService.calculateNewDueDate(
                subscription.getPaymentDueDate(), paymentDate, gracePeriodDays);

        subscription.setPaymentDueDate(newDueDate);
        subscription.setEndDate(newDueDate);
        subscription.setMonthsPaid((subscription.getMonthsPaid() != null ? subscription.getMonthsPaid() : 0L) + 1L);
        subscription.setStatus(SubStatus.ACTIVE);

        activateInventory(account, profile);

        Subscription saved = subscriptionRepositoryPort.save(subscription);
        recordRenewalNotification(saved, client);
        return saved;
    }

    private void activateInventory(Account account, Profile selectedProfile) {
        if (account.getSaleMode() == SaleMode.FULL_ACCOUNT) {
            var profiles = profileRepositoryPort.findByAccountId(account.getId());
            profiles.forEach(profile -> profile.setStatus(ProfileStatus.ACTIVE));
            profileRepositoryPort.saveAll(profiles);
            account.setStatus(AccountStatus.FULL);
            accountRepositoryPort.save(account);
            return;
        }

        selectedProfile.setStatus(ProfileStatus.ACTIVE);
        profileRepositoryPort.save(selectedProfile);
    }

    private void recordRenewalNotification(Subscription subscription, Client client) {
        String payload = String.format(
                "{\"subscriptionId\":\"%s\",\"clientId\":\"%s\",\"paymentDueDate\":\"%s\"}",
                subscription.getUuid(), client.getUuid(), subscription.getPaymentDueDate());
        notificationLogPort.record("SUBSCRIPTION_RENEWED", client.getEmail(), payload,
                "subscription", subscription.getId(), "renewed");
    }

    private Long resolveVendorId(String callerExternalId) {
        var user = userRepositoryPort.findByExternalId(callerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for externalId: " + callerExternalId));
        return vendorRepositoryPort.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor not found for userId: " + user.getId()))
                .getId();
    }
}
