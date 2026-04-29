package com.neversion.api.subscription.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.subscription.application.port.in.ListSubscriptionsUseCase;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * US-043: Lists subscriptions scoped to the authenticated vendor.
 * Enforces ADR-02 by resolving the caller's vendor from the JWT externalId.
 */
@Service
public class ListSubscriptionsService implements ListSubscriptionsUseCase {

    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;

    public ListSubscriptionsService(SubscriptionRepositoryPort subscriptionRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort) {
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> listByVendor(UUID vendorUuid, UUID serviceUuid, SubStatus status,
            String callerExternalId) {
        Vendor vendor = vendorRepositoryPort.findByUuid(vendorUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + vendorUuid));

        // ADR-02: the authenticated vendor can only list its own subscriptions.
        Long callerVendorId = resolveVendorId(callerExternalId);
        if (!callerVendorId.equals(vendor.getId())) {
            throw new AccessDeniedException("Access denied: you do not own vendor " + vendorUuid);
        }

        Long serviceId = null;
        if (serviceUuid != null) {
            serviceId = serviceRepositoryPort.findById(serviceUuid)
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + serviceUuid))
                    .getId();
        }

        return subscriptionRepositoryPort.findByVendorIdFiltered(vendor.getId(), serviceId, status);
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
