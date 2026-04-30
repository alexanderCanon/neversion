package com.neversion.api.assignment.application.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.assignment.application.port.in.DeliverAccessUseCase;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.subscription.domain.model.Subscription;

@Service
public class DeliverAccessService implements DeliverAccessUseCase {

    private final ProfileRepositoryPort profileRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final NotificationLogPort notificationLogPort;
    private final NotificationPayloadWriter payloadWriter;

    public DeliverAccessService(
            ProfileRepositoryPort profileRepositoryPort,
            AccountRepositoryPort accountRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            NotificationLogPort notificationLogPort,
            NotificationPayloadWriter payloadWriter) {
        this.profileRepositoryPort = profileRepositoryPort;
        this.accountRepositoryPort = accountRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.notificationLogPort = notificationLogPort;
        this.payloadWriter = payloadWriter;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deliver(Subscription subscription) {
        var profile = profileRepositoryPort.findByInternalId(subscription.getProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for subscription."));

        var account = accountRepositoryPort.findByInternalId(profile.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for profile."));

        var service = serviceRepositoryPort.findByInternalId(account.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found for account."));

        var client = clientRepositoryPort.findByInternalId(subscription.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found for subscription."));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("subscriptionId", subscription.getUuid());
        payload.put("serviceName", service.getName());
        payload.put("accountEmail", account.getEmail());
        payload.put("accountPassword", account.getPassword());
        payload.put("profileName", profile.getName());
        if (profile.getPin() != null) {
            payload.put("pin", profile.getPin());
        }
        payload.put("endDate", subscription.getEndDate() != null ? subscription.getEndDate().toString() : null);
        payload.put("clientName", client.getName());

        notificationLogPort.record("ACCESS_DELIVERED", client.getEmail(), payloadWriter.write(payload),
                "subscription", subscription.getId(), "access_delivered");
    }
}
