package com.neversion.api.assignment.application.service;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.assignment.application.port.in.DeliverAccessUseCase;
import com.neversion.api.assignment.application.port.in.ManualAssignmentUseCase;
import com.neversion.api.assignment.application.port.in.dto.AssignmentResult;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BadRequestException;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.ProfileAssignmentHistory;
import com.neversion.api.profile.domain.port.out.ProfileAssignmentHistoryRepositoryPort;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.shared.domain.model.enums.AccountStatus;

@Service
public class ManualAssignmentService implements ManualAssignmentUseCase {

    private static final Logger log = LoggerFactory.getLogger(ManualAssignmentService.class);

    private final ClientRepositoryPort clientRepositoryPort;
    private final ProfileRepositoryPort profileRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final DeliverAccessUseCase deliverAccessUseCase;
    private final AssignmentContextResolver contextResolver;
    private final ProfileAssignmentHistoryRepositoryPort historyRepositoryPort;

    public ManualAssignmentService(
            ClientRepositoryPort clientRepositoryPort,
            ProfileRepositoryPort profileRepositoryPort,
            AccountRepositoryPort accountRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort,
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            DeliverAccessUseCase deliverAccessUseCase,
            AssignmentContextResolver contextResolver,
            ProfileAssignmentHistoryRepositoryPort historyRepositoryPort) {
        this.clientRepositoryPort = clientRepositoryPort;
        this.profileRepositoryPort = profileRepositoryPort;
        this.accountRepositoryPort = accountRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.deliverAccessUseCase = deliverAccessUseCase;
        this.contextResolver = contextResolver;
        this.historyRepositoryPort = historyRepositoryPort;
    }

    @Override
    @Transactional
    public AssignmentResult assign(UUID clientUuid, UUID serviceUuid, UUID profileUuid,
            LocalDate startDate, LocalDate endDate, String callerExternalId) {
        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date must be on or after start date.");
        }

        var vendor = contextResolver.resolveCallerVendor(callerExternalId);

        var client = clientRepositoryPort.findById(clientUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientUuid));
        if (!Objects.equals(client.getVendorId(), vendor.getId())) {
            throw new AccessDeniedException("You do not have permission to assign access to this client.");
        }

        var service = serviceRepositoryPort.findById(serviceUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + serviceUuid));
        if (!Objects.equals(service.getVendorId(), vendor.getId())) {
            throw new AccessDeniedException("You do not have permission to assign this service.");
        }

        var profile = profileRepositoryPort.findById(profileUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + profileUuid));
        if (!Objects.equals(profile.getVendorId(), vendor.getId())) {
            throw new AccessDeniedException("You do not have permission to assign this profile.");
        }

        if (profile.getStatus() != ProfileStatus.AVAILABLE) {
            throw new BadRequestException("Selected profile must be AVAILABLE.");
        }

        if (subscriptionRepositoryPort.existsActiveByProfileId(profile.getId())) {
            throw new BusinessRuleException("Selected profile already has an active subscription.");
        }

        var account = accountRepositoryPort.findByInternalId(profile.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for profile."));

        if (!Objects.equals(account.getServiceId(), service.getId())) {
            throw new BadRequestException("Selected profile does not belong to the selected service.");
        }

        activateInventory(account, profile);

        Subscription savedSubscription = subscriptionRepositoryPort.save(Subscription.builder()
                .clientId(client.getId())
                .clientUuid(client.getUuid())
                .profileId(profile.getId())
                .profileUuid(profile.getUuid())
                .accountUuid(account.getUuid())
                .orderId(null)
                .serviceId(service.getId())
                .startDate(startDate)
                .endDate(endDate)
                .paymentDueDate(endDate)
                .monthsPaid(1L)
                .saleMode(account.getSaleMode())
                .status(SubStatus.ACTIVE)
                .vendorId(vendor.getId())
                .build());

        queueAccessDelivery(savedSubscription);

        historyRepositoryPort.save(ProfileAssignmentHistory.builder()
                .profileId(profile.getId())
                .subscriptionId(savedSubscription.getId())
                .accountEmail(account.getEmail())
                .accountPassword(account.getPassword())
                .profileName(profile.getName())
                .profilePin(profile.getPin())
                .profileNotes(profile.getNotes())
                .vendorId(vendor.getId())
                .build());

        return new AssignmentResult(
                savedSubscription.getUuid(),
                null,
                profile.getUuid(),
                client.getUuid(),
                service.getName(),
                startDate,
                endDate,
                true);
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

    private void activateInventory(com.neversion.api.account.domain.model.Account account, Profile selectedProfile) {
        if (account.getSaleMode() == SaleMode.FULL_ACCOUNT) {
            if (!Boolean.TRUE.equals(selectedProfile.getIsOwner())) {
                throw new BadRequestException("Full account assignments must use the owner profile.");
            }
            if (account.getStatus() != AccountStatus.AVAILABLE) {
                throw new BadRequestException("Selected account must be AVAILABLE.");
            }

            var profiles = profileRepositoryPort.findByAccountId(account.getId());
            boolean hasUnavailableProfile = profiles.stream()
                    .anyMatch(profile -> profile.getStatus() != ProfileStatus.AVAILABLE);
            if (hasUnavailableProfile) {
                throw new BadRequestException("All profiles must be AVAILABLE for a full account assignment.");
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
}
