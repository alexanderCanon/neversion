package com.neversion.api.subscription.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.application.service.VendorSecurityService;
import com.neversion.api.subscription.application.port.in.ListSubscriptionsUseCase;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.model.SubscriptionListView;
import com.neversion.api.subscription.domain.model.enums.SubStatus;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
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
    private final ServiceRepositoryPort serviceRepositoryPort;
    private final VendorSecurityService vendorSecurityService;

    public ListSubscriptionsService(SubscriptionRepositoryPort subscriptionRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort,
            VendorSecurityService vendorSecurityService) {
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.vendorSecurityService = vendorSecurityService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> listByVendor(UUID vendorUuid, UUID serviceUuid, SubStatus status,
            String callerExternalId) {
        Vendor vendor = resolveOwnedVendor(vendorUuid, callerExternalId);
        Long serviceId = resolveServiceId(serviceUuid);
        return subscriptionRepositoryPort.findByVendorIdFiltered(vendor.getId(), serviceId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionListView> listViewsByVendor(UUID vendorUuid, UUID serviceUuid, SubStatus status,
            String callerExternalId) {
        Vendor vendor = resolveOwnedVendor(vendorUuid, callerExternalId);
        Long serviceId = resolveServiceId(serviceUuid);
        return subscriptionRepositoryPort.findVendorSubscriptionViews(vendor.getId(), serviceId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionListView> listViews(UUID serviceUuid, SubStatus status, String callerExternalId) {
        Long callerVendorId = vendorSecurityService.resolveVendorId(callerExternalId);
        Long serviceId = resolveServiceId(serviceUuid);
        return subscriptionRepositoryPort.findVendorSubscriptionViews(callerVendorId, serviceId, status);
    }


    /**
     * Resolves the route vendor and enforces ADR-02: the authenticated caller
     * can only access its own vendor.
     */
    private Vendor resolveOwnedVendor(UUID vendorUuid, String callerExternalId) {
        Vendor vendor = vendorRepositoryPort.findByUuid(vendorUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + vendorUuid));

        Long callerVendorId = vendorSecurityService.resolveVendorId(callerExternalId);
        vendorSecurityService.assertOwnership(callerVendorId, vendor.getId(), "vendor " + vendorUuid);
        return vendor;
    }

    private Long resolveServiceId(UUID serviceUuid) {
        if (serviceUuid == null) {
            return null;
        }
        return serviceRepositoryPort.findById(serviceUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + serviceUuid))
                .getId();
    }
}
