package com.neversion.api.subscription.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.account.domain.model.Account;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.port.out.OrderRepositoryPort;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.application.service.VendorSecurityService;
import com.neversion.api.subscription.application.port.in.GetSubscriptionDetailUseCase;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;

/**
 * US-044: Subscription detail with commercial origin and financial snapshots.
 */
@Service
public class GetSubscriptionDetailService implements GetSubscriptionDetailUseCase {

    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final ProfileRepositoryPort profileRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final VendorSecurityService vendorSecurityService;

    public GetSubscriptionDetailService(
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            ProfileRepositoryPort profileRepositoryPort,
            AccountRepositoryPort accountRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort,
            OrderRepositoryPort orderRepositoryPort,
            VendorSecurityService vendorSecurityService) {
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.profileRepositoryPort = profileRepositoryPort;
        this.accountRepositoryPort = accountRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.orderRepositoryPort = orderRepositoryPort;
        this.vendorSecurityService = vendorSecurityService;
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionDetail getDetail(UUID subscriptionUuid, String callerExternalId) {
        Subscription subscription = subscriptionRepositoryPort.findById(subscriptionUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found with id: " + subscriptionUuid));

        Long callerVendorId = vendorSecurityService.resolveVendorId(callerExternalId);
        vendorSecurityService.assertOwnership(callerVendorId, subscription.getVendorId(),
                "subscription " + subscriptionUuid);

        Client client = clientRepositoryPort.findByInternalId(subscription.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found for subscription: " + subscriptionUuid));
        Profile profile = profileRepositoryPort.findByInternalId(subscription.getProfileId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile not found for subscription: " + subscriptionUuid));
        Account account = accountRepositoryPort.findByInternalId(profile.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found for profile: " + profile.getUuid()));
        var service = serviceRepositoryPort.findByInternalId(account.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Service not found for account: " + account.getUuid()));

        Order order = null;
        if (subscription.getOrderId() != null) {
            order = orderRepositoryPort.findByInternalId(subscription.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Order not found for subscription: " + subscriptionUuid));
        }

        return new SubscriptionDetail(subscription, client, profile, account, service, order);
    }
}
