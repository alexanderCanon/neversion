package com.neversion.api.assignment.application.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.assignment.application.port.in.SuggestAssignmentUseCase;
import com.neversion.api.assignment.application.port.in.dto.AssignmentSuggestion;
import com.neversion.api.exception.BadRequestException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.shared.domain.model.enums.AccountStatus;
import com.neversion.api.shared.port.out.NotificationLogPort;

@Service
public class SuggestAssignmentService implements SuggestAssignmentUseCase {

    private static final Logger log = LoggerFactory.getLogger(SuggestAssignmentService.class);

    private final OrderRepositoryPort orderRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final ProfileRepositoryPort profileRepositoryPort;
    private final NotificationLogPort notificationLogPort;
    private final AssignmentContextResolver contextResolver;
    private final NotificationPayloadWriter payloadWriter;

    public SuggestAssignmentService(
            OrderRepositoryPort orderRepositoryPort,
            AccountRepositoryPort accountRepositoryPort,
            ProfileRepositoryPort profileRepositoryPort,
            NotificationLogPort notificationLogPort,
            AssignmentContextResolver contextResolver,
            NotificationPayloadWriter payloadWriter) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.accountRepositoryPort = accountRepositoryPort;
        this.profileRepositoryPort = profileRepositoryPort;
        this.notificationLogPort = notificationLogPort;
        this.contextResolver = contextResolver;
        this.payloadWriter = payloadWriter;
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentSuggestion suggest(UUID orderUuid, String callerExternalId) {
        var vendor = contextResolver.resolveCallerVendor(callerExternalId);
        var order = orderRepositoryPort.findByUuid(orderUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderUuid));

        contextResolver.ensureOrderOwnership(order, vendor);

        if (order.getStatus() != OrderStatus.VALIDATED) {
            throw new BadRequestException("Order must be VALIDATED before assignment suggestion.");
        }

        var service = contextResolver.resolveSingleServiceForOrder(order);
        List<Account> accounts = accountRepositoryPort.findByServiceIdAndVendorId(service.getId(), vendor.getId());

        for (Account account : accounts) {
            if (account.getSaleMode() != SaleMode.BY_PROFILE) {
                continue;
            }

            List<Profile> profiles = profileRepositoryPort.findAvailableByAccountId(account.getId());
            for (Profile profile : profiles) {
                if (profile.getStatus() == ProfileStatus.AVAILABLE) {
                    return new AssignmentSuggestion(
                            true,
                            SaleMode.BY_PROFILE,
                            profile.getUuid(),
                            account.getUuid(),
                            service.getName(),
                            account.getEmail(),
                            null);
                }
            }
        }

        for (Account account : accounts) {
            if (account.getSaleMode() != SaleMode.FULL_ACCOUNT || account.getStatus() != AccountStatus.AVAILABLE) {
                continue;
            }

            var ownerProfile = profileRepositoryPort.findByAccountId(account.getId()).stream()
                    .filter(profile -> Boolean.TRUE.equals(profile.getIsOwner()))
                    .filter(profile -> profile.getStatus() == ProfileStatus.AVAILABLE)
                    .findFirst();

            if (ownerProfile.isPresent()) {
                return new AssignmentSuggestion(
                        true,
                        SaleMode.FULL_ACCOUNT,
                        ownerProfile.get().getUuid(),
                        account.getUuid(),
                        service.getName(),
                        account.getEmail(),
                        null);
            }

            return new AssignmentSuggestion(
                    false,
                    SaleMode.FULL_ACCOUNT,
                    null,
                    account.getUuid(),
                    service.getName(),
                    account.getEmail(),
                    "FULL_ACCOUNT_OWNER_PROFILE_MISSING");
        }

        recordNoInventoryAlert(vendor.getId(), order.getUuid(), service.getId());

        return new AssignmentSuggestion(
                false,
                SaleMode.BY_PROFILE,
                null,
                null,
                service.getName(),
                null,
                "NO_AVAILABLE_PROFILE");
    }

    private void recordNoInventoryAlert(Long vendorId, UUID orderUuid, Long serviceId) {
        try {
            String payload = payloadWriter.write(Map.of(
                    "vendorId", vendorId,
                    "orderId", orderUuid,
                    "serviceId", serviceId));
            notificationLogPort.record("NO_INVENTORY_ALERT", "vendor:" + vendorId, payload,
                    "vendor", vendorId, "no_inventory");
        } catch (RuntimeException ex) {
            log.warn("Unable to record no-inventory alert for order {}.", orderUuid, ex);
        }
    }
}
