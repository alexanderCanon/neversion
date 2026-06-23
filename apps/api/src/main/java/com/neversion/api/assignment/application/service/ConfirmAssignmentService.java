package com.neversion.api.assignment.application.service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.assignment.application.port.in.ConfirmAssignmentUseCase;
import com.neversion.api.assignment.application.port.in.DeliverAccessUseCase;
import com.neversion.api.assignment.application.port.in.dto.AssignmentResult;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BadRequestException;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.order.domain.model.OrderStatusChange;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;
import com.neversion.api.order.domain.port.out.OrderStatusHistoryPort;
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
public class ConfirmAssignmentService implements ConfirmAssignmentUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConfirmAssignmentService.class);

    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderStatusHistoryPort orderStatusHistoryPort;
    private final ProfileRepositoryPort profileRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final DeliverAccessUseCase deliverAccessUseCase;
    private final AssignmentContextResolver contextResolver;
    private final ProfileAssignmentHistoryRepositoryPort historyRepositoryPort;

    public ConfirmAssignmentService(
            OrderRepositoryPort orderRepositoryPort,
            OrderStatusHistoryPort orderStatusHistoryPort,
            ProfileRepositoryPort profileRepositoryPort,
            AccountRepositoryPort accountRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            DeliverAccessUseCase deliverAccessUseCase,
            AssignmentContextResolver contextResolver,
            ProfileAssignmentHistoryRepositoryPort historyRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.orderStatusHistoryPort = orderStatusHistoryPort;
        this.profileRepositoryPort = profileRepositoryPort;
        this.accountRepositoryPort = accountRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.deliverAccessUseCase = deliverAccessUseCase;
        this.contextResolver = contextResolver;
        this.historyRepositoryPort = historyRepositoryPort;
    }

    @Override
    @Transactional
    public AssignmentResult confirm(UUID orderUuid, UUID profileUuid, String callerExternalId) {
        var vendor = contextResolver.resolveCallerVendor(callerExternalId);
        var order = orderRepositoryPort.findByUuid(orderUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderUuid));

        contextResolver.ensureOrderOwnership(order, vendor);

        if (order.getStatus() != OrderStatus.VALIDATED) {
            throw new BadRequestException("Order must be VALIDATED before assignment confirmation.");
        }

        subscriptionRepositoryPort.findByOrderId(order.getId())
                .ifPresent(existing -> {
                    throw new BusinessRuleException("Assignment already confirmed for this order.");
                });

        var profile = profileRepositoryPort.findById(profileUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + profileUuid));

        if (!Objects.equals(profile.getVendorId(), vendor.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to assign this profile.");
        }

        if (profile.getStatus() != ProfileStatus.AVAILABLE) {
            throw new BadRequestException("Selected profile must be AVAILABLE.");
        }

        var account = accountRepositoryPort.findByInternalId(profile.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for profile."));

        var orderService = contextResolver.resolveSingleServiceForOrder(order);
        if (!Objects.equals(account.getServiceId(), orderService.getId())) {
            throw new BadRequestException("Selected profile does not belong to the ordered service.");
        }

        var service = serviceRepositoryPort.findByInternalId(account.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found for profile."));

        if (service.getDurationDays() == null) {
            throw new BadRequestException("Service has no duration configured.");
        }

        if (order.getApprovedAt() == null) {
            throw new BadRequestException("Order has no approval timestamp.");
        }

        var client = clientRepositoryPort.findByInternalId(order.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found for order."));

        LocalDate startDate = LocalDate.ofInstant(order.getApprovedAt(), ZoneOffset.UTC);
        LocalDate endDate = startDate.plusDays(service.getDurationDays());

        activateInventory(account, profile);

        Subscription savedSubscription = subscriptionRepositoryPort.save(Subscription.builder()
                .clientId(client.getId())
                .clientUuid(client.getUuid())
                .profileId(profile.getId())
                .profileUuid(profile.getUuid())
                .accountUuid(account.getUuid())
                .orderId(order.getId())
                .serviceId(service.getId())
                .startDate(startDate)
                .endDate(endDate)
                .paymentDueDate(endDate)
                .monthsPaid(1L)
                .priceSold(order.getTotal())
                .discountApplied(order.getDiscount())
                .saleMode(account.getSaleMode())
                .accountPreference(order.getAccountPreference())
                .status(SubStatus.ACTIVE)
                .vendorId(vendor.getId())
                .build());

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.COMPLETED);
        orderRepositoryPort.save(order);

        orderStatusHistoryPort.record(OrderStatusChange.builder()
                .orderId(order.getId())
                .oldStatus(oldStatus)
                .newStatus(OrderStatus.COMPLETED)
                .changedBy(callerExternalId)
                .notes("Assignment confirmed")
                .changedAt(java.time.Instant.now())
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
                order.getUuid(),
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
                log.error("Access delivery failed for subscription {}.", subscription.getUuid(), ex);
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
