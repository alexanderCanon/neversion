package com.neversion.api.subscription.application.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.model.Service;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.application.service.VendorSecurityService;
import com.neversion.api.subscription.application.port.in.BatchCreateSubscriptionsUseCase;
import com.neversion.api.subscription.application.port.in.CreateManualSubscriptionUseCase;
import com.neversion.api.subscription.domain.model.Subscription;

/**
 * Batch creation of manual subscriptions for a single client across multiple
 * services. Supports auto-assignment of available profiles and manual override.
 * <p>
 * Each item runs in its own transaction (delegated to
 * {@link CreateManualSubscriptionUseCase#create}), so partial success is
 * possible. Failed items are reported individually without rolling back
 * successful ones.
 */
@org.springframework.stereotype.Service
public class BatchCreateSubscriptionsService implements BatchCreateSubscriptionsUseCase {

    private static final Logger log = LoggerFactory.getLogger(BatchCreateSubscriptionsService.class);

    private final CreateManualSubscriptionUseCase createManualSubscriptionUseCase;
    private final ProfileRepositoryPort profileRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;
    private final VendorSecurityService vendorSecurityService;

    public BatchCreateSubscriptionsService(
            CreateManualSubscriptionUseCase createManualSubscriptionUseCase,
            ProfileRepositoryPort profileRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort,
            VendorSecurityService vendorSecurityService) {
        this.createManualSubscriptionUseCase = createManualSubscriptionUseCase;
        this.profileRepositoryPort = profileRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.vendorSecurityService = vendorSecurityService;
    }

    @Override
    public BatchResult create(BatchCommand command, String callerExternalId) {
        Long vendorId = vendorSecurityService.resolveVendorId(callerExternalId);

        Set<UUID> claimedProfileUuids = new HashSet<>();
        List<BatchItemResult> results = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;

        for (BatchItemCommand item : command.items()) {
            int quantity = Math.max(1, item.quantity());
            for (int i = 0; i < quantity; i++) {
                BatchItemResult result = createSingle(
                        command, item, vendorId, claimedProfileUuids, callerExternalId);
                results.add(result);
                if (result.success()) {
                    successCount++;
                } else {
                    failedCount++;
                }
            }
        }

        return new BatchResult(results.size(), successCount, failedCount, results);
    }

    private BatchItemResult createSingle(BatchCommand command, BatchItemCommand item,
            Long vendorId, Set<UUID> claimedProfileUuids, String callerExternalId) {
        try {
            UUID profileUuid = item.profileUuid();

            if (profileUuid == null) {
                profileUuid = autoAssignProfile(item.serviceUuid(), vendorId, claimedProfileUuids);
                if (profileUuid == null) {
                    return new BatchItemResult(item.serviceUuid(), false, null,
                            "No hay perfiles disponibles para este servicio.");
                }
            } else {
                claimedProfileUuids.add(profileUuid);
            }

            Subscription subscription = Subscription.builder()
                    .clientUuid(command.clientUuid())
                    .profileUuid(profileUuid)
                    .serviceUuid(item.serviceUuid())
                    .paymentDueDate(command.paymentDueDate())
                    .priceSold(item.priceSold())
                    .discountApplied(command.discountApplied())
                    .notes(command.notes())
                    .build();

            Subscription created = createManualSubscriptionUseCase.create(
                    subscription, command.sendNotification(), callerExternalId);

            return new BatchItemResult(item.serviceUuid(), true, created.getUuid(), null);
        } catch (RuntimeException ex) {
            log.warn("Batch item failed for service {}: {}", item.serviceUuid(), ex.getMessage());
            return new BatchItemResult(item.serviceUuid(), false, null, ex.getMessage());
        }
    }

    private UUID autoAssignProfile(UUID serviceUuid, Long vendorId, Set<UUID> claimedProfileUuids) {
        Service service = serviceRepositoryPort.findById(serviceUuid)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Service not found: " + serviceUuid));

        List<Profile> available = profileRepositoryPort
                .findAvailableByServiceIdAndVendorId(service.getId(), vendorId);

        return available.stream()
                .map(Profile::getUuid)
                .filter(uuid -> !claimedProfileUuids.contains(uuid))
                .findFirst()
                .orElse(null);
    }
}
