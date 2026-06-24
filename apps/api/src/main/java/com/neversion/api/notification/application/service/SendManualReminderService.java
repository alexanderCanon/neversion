package com.neversion.api.notification.application.service;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.notification.application.port.in.SendManualReminderUseCase;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.application.service.VendorSecurityService;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;

@Service
public class SendManualReminderService implements SendManualReminderUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendManualReminderService.class);
    private static final String ENTITY_TYPE = "subscription";
    private static final String NOTIFICATION_TYPE = "RENEWAL_REMINDER_MANUAL";

    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final ProfileRepositoryPort profileRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;
    private final NotificationLogPort notificationLogPort;
    private final VendorSecurityService vendorSecurityService;
    private final ObjectMapper objectMapper;

    public SendManualReminderService(
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            ProfileRepositoryPort profileRepositoryPort,
            AccountRepositoryPort accountRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort,
            NotificationLogPort notificationLogPort,
            VendorSecurityService vendorSecurityService,
            ObjectMapper objectMapper) {
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.profileRepositoryPort = profileRepositoryPort;
        this.accountRepositoryPort = accountRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.notificationLogPort = notificationLogPort;
        this.vendorSecurityService = vendorSecurityService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void sendReminder(UUID subscriptionId, String callerExternalId) {
        Subscription subscription = subscriptionRepositoryPort.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found: " + subscriptionId));

        Long callerVendorId = vendorSecurityService.resolveVendorId(callerExternalId);
        vendorSecurityService.assertOwnership(
                callerVendorId, subscription.getVendorId(), "subscription " + subscriptionId);

        String clientEmail = clientRepositoryPort.findByInternalId(subscription.getClientId())
                .map(c -> c.getEmail())
                .orElse(null);

        if (clientEmail == null || clientEmail.isBlank()) {
            throw new BusinessRuleException("Client has no email address for reminder");
        }

        String payload = buildPayload(subscription);
        String stage = "manual_" + System.currentTimeMillis();

        notificationLogPort.record(
                NOTIFICATION_TYPE,
                clientEmail,
                payload,
                ENTITY_TYPE,
                subscription.getId(),
                stage);

        log.info("Recorded manual renewal reminder for subscription {} (vendor {})",
                subscription.getUuid(), callerVendorId);
    }

    private String buildPayload(Subscription subscription) {
        String clientName = clientRepositoryPort.findByInternalId(subscription.getClientId())
                .map(c -> c.getName())
                .orElse("Cliente");

        String serviceName = resolveServiceName(subscription);

        try {
            return objectMapper.writeValueAsString(Map.of(
                    "subscriptionId", subscription.getUuid().toString(),
                    "clientName", clientName != null ? clientName : "Cliente",
                    "serviceName", serviceName != null ? serviceName : "Servicio",
                    "paymentDueDate", subscription.getPaymentDueDate() != null
                            ? subscription.getPaymentDueDate().toString() : "",
                    "manual", true));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize manual reminder payload", e);
        }
    }

    private String resolveServiceName(Subscription subscription) {
        try {
            if (subscription.getServiceId() != null) {
                return serviceRepositoryPort.findByInternalId(subscription.getServiceId())
                        .map(s -> s.getName())
                        .orElse(null);
            }
            var profile = profileRepositoryPort.findByInternalId(subscription.getProfileId()).orElse(null);
            if (profile == null) return null;
            var account = accountRepositoryPort.findByInternalId(profile.getAccountId()).orElse(null);
            if (account == null) return null;
            return serviceRepositoryPort.findByInternalId(account.getServiceId())
                    .map(s -> s.getName())
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
