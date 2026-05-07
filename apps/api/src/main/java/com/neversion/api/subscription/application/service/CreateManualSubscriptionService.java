package com.neversion.api.subscription.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.assignment.application.port.in.DeliverAccessUseCase;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BadRequestException;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.domain.model.enums.AccountStatus;
import com.neversion.api.subscription.application.port.in.CreateManualSubscriptionUseCase;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * US-048: Creates manual subscriptions for external sales or migration.
 */
@Service
public class CreateManualSubscriptionService implements CreateManualSubscriptionUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateManualSubscriptionService.class);

    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final ProfileRepositoryPort profileRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final DeliverAccessUseCase deliverAccessUseCase;
    private final Clock clock;

    public CreateManualSubscriptionService(
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            ProfileRepositoryPort profileRepositoryPort,
            AccountRepositoryPort accountRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            DeliverAccessUseCase deliverAccessUseCase,
            Clock clock) {
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.profileRepositoryPort = profileRepositoryPort;
        this.accountRepositoryPort = accountRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.deliverAccessUseCase = deliverAccessUseCase;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Subscription create(Subscription subscription, boolean sendNotification, String callerExternalId) {
        Long vendorId = resolveVendorId(callerExternalId);

        Client client = clientRepositoryPort.findById(subscription.getClientUuid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found: " + subscription.getClientUuid()));
        ensureOwned(client.getVendorId(), vendorId, "client");

        var service = serviceRepositoryPort.findById(subscription.getServiceUuid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Service not found: " + subscription.getServiceUuid()));
        ensureOwned(service.getVendorId(), vendorId, "service");

        Profile profile = profileRepositoryPort.findById(subscription.getProfileUuid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile not found: " + subscription.getProfileUuid()));
        ensureOwned(profile.getVendorId(), vendorId, "profile");

        if (profile.getStatus() != ProfileStatus.AVAILABLE) {
            throw new BusinessRuleException("Selected profile must be AVAILABLE.");
        }

        if (subscriptionRepositoryPort.existsActiveByProfileId(profile.getId())) {
            throw new BusinessRuleException("Selected profile already has an active subscription.");
        }

        Account account = accountRepositoryPort.findByInternalId(profile.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found for profile: " + profile.getUuid()));

        if (!Objects.equals(account.getServiceId(), service.getId())) {
            throw new BadRequestException("Selected profile does not belong to the selected service.");
        }

        LocalDate startDate = LocalDate.now(clock);
        if (subscription.getPaymentDueDate().isBefore(startDate)) {
            throw new BadRequestException("Payment due date must be today or later.");
        }

        activateInventory(account, profile);

        Subscription toSave = Subscription.builder()
                .clientId(client.getId())
                .clientUuid(client.getUuid())
                .profileId(profile.getId())
                .profileUuid(profile.getUuid())
                .accountUuid(account.getUuid())
                .orderId(null)
                .serviceId(service.getId())
                .serviceUuid(service.getUuid())
                .startDate(startDate)
                .endDate(subscription.getPaymentDueDate())
                .paymentDueDate(subscription.getPaymentDueDate())
                .monthsPaid(1L)
                .priceSold(subscription.getPriceSold())
                .discountApplied(subscription.getDiscountApplied())
                .saleMode(account.getSaleMode())
                .status(SubStatus.ACTIVE)
                .notes(subscription.getNotes())
                .vendorId(vendorId)
                .build();

        Subscription saved = subscriptionRepositoryPort.save(toSave);
        if (sendNotification) {
            queueAccessDelivery(saved);
        }
        return saved;
    }

    private void activateInventory(Account account, Profile selectedProfile) {
        if (account.getSaleMode() == SaleMode.FULL_ACCOUNT) {
            if (!Boolean.TRUE.equals(selectedProfile.getIsOwner())) {
                throw new BadRequestException("Full account subscriptions must use the owner profile.");
            }
            if (account.getStatus() != AccountStatus.AVAILABLE) {
                throw new BusinessRuleException("Selected account must be AVAILABLE.");
            }

            var profiles = profileRepositoryPort.findByAccountId(account.getId());
            boolean hasUnavailableProfile = profiles.stream()
                    .anyMatch(profile -> profile.getStatus() != ProfileStatus.AVAILABLE);
            if (hasUnavailableProfile) {
                throw new BusinessRuleException("All profiles must be AVAILABLE for a full account subscription.");
            }

            profiles.forEach(profile -> profile.setStatus(ProfileStatus.ACTIVE));
            profileRepositoryPort.saveAll(profiles);
            account.setStatus(AccountStatus.FULL);
            accountRepositoryPort.save(account);
            return;
        }

        selectedProfile.setStatus(ProfileStatus.ACTIVE);
        profileRepositoryPort.save(selectedProfile);
    }

    private void queueAccessDelivery(Subscription subscription) {
        Runnable delivery = () -> {
            try {
                deliverAccessUseCase.deliver(subscription);
            } catch (RuntimeException ex) {
                log.error("Access delivery failed for manual subscription {}.", subscription.getUuid(), ex);
            }
        };

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    delivery.run();
                }
            });
            return;
        }

        delivery.run();
    }

    private void ensureOwned(Long resourceVendorId, Long callerVendorId, String resourceName) {
        if (!Objects.equals(resourceVendorId, callerVendorId)) {
            throw new AccessDeniedException("You do not have permission to use this " + resourceName + ".");
        }
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
