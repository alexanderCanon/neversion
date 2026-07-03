package com.neversion.api.subscription.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.neversion.api.profile.domain.model.ProfileAssignmentHistory;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileAssignmentHistoryRepositoryPort;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.application.service.VendorSecurityService;
import com.neversion.api.shared.domain.model.enums.AccountStatus;
import com.neversion.api.subscription.application.port.in.CreateManualSubscriptionUseCase;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;

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
    private final DeliverAccessUseCase deliverAccessUseCase;
    private final VendorSecurityService vendorSecurityService;
    private final ProfileAssignmentHistoryRepositoryPort historyRepositoryPort;
    private final Clock clock;

    public CreateManualSubscriptionService(
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            ProfileRepositoryPort profileRepositoryPort,
            AccountRepositoryPort accountRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort,
            DeliverAccessUseCase deliverAccessUseCase,
            VendorSecurityService vendorSecurityService,
            ProfileAssignmentHistoryRepositoryPort historyRepositoryPort,
            Clock clock) {
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.profileRepositoryPort = profileRepositoryPort;
        this.accountRepositoryPort = accountRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.deliverAccessUseCase = deliverAccessUseCase;
        this.vendorSecurityService = vendorSecurityService;
        this.historyRepositoryPort = historyRepositoryPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Subscription create(Subscription subscription, boolean sendNotification, String callerExternalId) {
        Long vendorId = vendorSecurityService.resolveVendorId(callerExternalId);

        Client client = clientRepositoryPort.findById(subscription.getClientUuid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found: " + subscription.getClientUuid()));
        vendorSecurityService.assertOwnership(vendorId, client.getVendorId(), "client");

        var service = serviceRepositoryPort.findById(subscription.getServiceUuid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Service not found: " + subscription.getServiceUuid()));
        vendorSecurityService.assertOwnership(vendorId, service.getVendorId(), "service");

        Profile profile = profileRepositoryPort.findById(subscription.getProfileUuid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile not found: " + subscription.getProfileUuid()));
        vendorSecurityService.assertOwnership(vendorId, profile.getVendorId(), "profile");

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

        LocalDate startDate = subscription.getStartDate() != null ? subscription.getStartDate() : LocalDate.now(clock);
        if (subscription.getPaymentDueDate().isBefore(startDate)) {
            throw new BadRequestException("Payment due date must be on or after start date.");
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

        historyRepositoryPort.save(ProfileAssignmentHistory.builder()
                .profileId(profile.getId())
                .subscriptionId(saved.getId())
                .accountEmail(account.getEmail())
                .accountPassword(account.getPassword())
                .profileName(profile.getName())
                .profilePin(profile.getPin())
                .profileNotes(profile.getNotes())
                .vendorId(vendorId)
                .build());

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

}
