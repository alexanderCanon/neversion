package com.neversion.api.notification.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.notification.application.port.in.SendRenewalRemindersUseCase;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.account.domain.port.out.AccountRepositoryPort;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.subscription.domain.model.Subscription;
import com.neversion.api.subscription.domain.port.out.SubscriptionRepositoryPort;
import com.neversion.api.user.application.port.out.AuthServicePort;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

/**
 * EPIC-08 US-054: Sends renewal reminder notifications for subscriptions
 * due in 7, 3, or 1 days.
 *
 * Each subscription triggers up to two notification_log records:
 *   - One for the client  (stage = "reminder_Xd_client") — only if client has an email.
 *   - One for the vendor  (stage = "reminder_Xd_vendor") — always if vendor email is resolvable.
 *
 * Separate stages guarantee independent deduplication: a client record never
 * blocks the vendor record and vice-versa.
 *
 * Uses notification_log deduplication (entity_type + entity_id + stage) to
 * avoid sending duplicates across cron executions.
 */
@Service
public class SendRenewalRemindersService implements SendRenewalRemindersUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendRenewalRemindersService.class);

    /** Reminder intervals: days before due date → stage base identifier. */
    private static final Map<Integer, String> REMINDER_INTERVALS = Map.of(
            7, "reminder_7d",
            3, "reminder_3d",
            1, "reminder_1d"
    );

    /** Notification type mapping: stage base → notification type. */
    private static final Map<String, String> STAGE_TO_TYPE = Map.of(
            "reminder_7d", "RENEWAL_REMINDER_7D",
            "reminder_3d", "RENEWAL_REMINDER_3D",
            "reminder_1d", "RENEWAL_REMINDER_1D"
    );

    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final ProfileRepositoryPort profileRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final AuthServicePort authServicePort;
    private final NotificationLogPort notificationLogPort;
    private final Clock clock;

    public SendRenewalRemindersService(
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            ProfileRepositoryPort profileRepositoryPort,
            AccountRepositoryPort accountRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            AuthServicePort authServicePort,
            NotificationLogPort notificationLogPort,
            Clock clock) {
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.profileRepositoryPort = profileRepositoryPort;
        this.accountRepositoryPort = accountRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.authServicePort = authServicePort;
        this.notificationLogPort = notificationLogPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public int sendReminders() {
        LocalDate today = LocalDate.now(clock);
        int totalSent = 0;

        for (var entry : REMINDER_INTERVALS.entrySet()) {
            int daysAhead = entry.getKey();
            String stageBase = entry.getValue();
            String notificationType = STAGE_TO_TYPE.get(stageBase);

            LocalDate targetDate = today.plusDays(daysAhead);
            List<Subscription> dueSubscriptions = subscriptionRepositoryPort.findActiveByPaymentDueDate(targetDate);

            for (Subscription sub : dueSubscriptions) {
                try {
                    String payload = buildPayload(sub, daysAhead);
                    String clientEmail = resolveClientEmail(sub);
                    String vendorEmail = resolveVendorEmail(sub);

                    boolean anyNotified = false;

                    // Notify client if they have an email
                    if (clientEmail != null) {
                        String clientStage = stageBase + "_client";
                        if (!notificationLogPort.existsByEntityAndStage("subscription", sub.getId(), clientStage)) {
                            notificationLogPort.record(notificationType, clientEmail, payload,
                                    "subscription", sub.getId(), clientStage);
                            totalSent++;
                            anyNotified = true;
                        }
                    }

                    // Always notify vendor if their email is resolvable
                    if (vendorEmail != null) {
                        String vendorStage = stageBase + "_vendor";
                        if (!notificationLogPort.existsByEntityAndStage("subscription", sub.getId(), vendorStage)) {
                            notificationLogPort.record(notificationType, vendorEmail, payload,
                                    "subscription", sub.getId(), vendorStage);
                            totalSent++;
                            anyNotified = true;
                        }
                    }

                    if (!anyNotified && clientEmail == null && vendorEmail == null) {
                        log.warn("Cannot send reminder for subscription {}: neither client nor vendor email found",
                                sub.getUuid());
                    }

                } catch (Exception e) {
                    log.error("Error recording reminder for subscription {}: {}",
                            sub.getUuid(), e.getMessage());
                }
            }
        }

        if (totalSent > 0) {
            log.info("Recorded {} renewal reminders", totalSent);
        }
        return totalSent;
    }

    private String buildPayload(Subscription sub, int daysRemaining) {
        String serviceName = resolveServiceName(sub);
        String clientName = resolveClientName(sub);

        return String.format(
                "{\"subscriptionId\":\"%s\",\"clientName\":\"%s\",\"serviceName\":\"%s\"," +
                "\"paymentDueDate\":\"%s\",\"daysRemaining\":%d}",
                sub.getUuid(),
                clientName != null ? clientName : "Cliente",
                serviceName != null ? serviceName : "Servicio",
                sub.getPaymentDueDate(),
                daysRemaining);
    }

    private String resolveClientEmail(Subscription sub) {
        return clientRepositoryPort.findByInternalId(sub.getClientId())
                .map(c -> c.getEmail())
                .orElse(null);
    }

    private String resolveClientName(Subscription sub) {
        return clientRepositoryPort.findByInternalId(sub.getClientId())
                .map(c -> c.getName())
                .orElse(null);
    }

    /**
     * Resolves the vendor email for a subscription by traversing:
     * Subscription → Profile → Account → Vendor → User → Auth email.
     */
    private String resolveVendorEmail(Subscription sub) {
        try {
            return profileRepositoryPort.findByInternalId(sub.getProfileId())
                    .flatMap(profile -> accountRepositoryPort.findByInternalId(profile.getAccountId()))
                    .flatMap(account -> vendorRepositoryPort.findByInternalId(account.getVendorId()))
                    .flatMap(vendor -> userRepositoryPort.findById(vendor.getUserId()))
                    .flatMap(user -> authServicePort.findEmailByExternalId(user.getExternalId()))
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Could not resolve vendor email for subscription {}: {}", sub.getUuid(), e.getMessage());
            return null;
        }
    }

    private String resolveServiceName(Subscription sub) {
        try {
            var profile = profileRepositoryPort.findByInternalId(sub.getProfileId()).orElse(null);
            if (profile == null) return null;
            var account = accountRepositoryPort.findByInternalId(profile.getAccountId()).orElse(null);
            if (account == null) return null;
            var service = serviceRepositoryPort.findByInternalId(account.getServiceId()).orElse(null);
            return service != null ? service.getName() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
