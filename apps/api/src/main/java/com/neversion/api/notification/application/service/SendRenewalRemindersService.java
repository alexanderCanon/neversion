package com.neversion.api.notification.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

/**
 * EPIC-08 US-054: Sends renewal reminder notifications for subscriptions
 * due in 7, 3, or 1 days. Uses notification_log deduplication
 * (entity_type + entity_id + stage) to avoid sending duplicates.
 */
@Service
public class SendRenewalRemindersService implements SendRenewalRemindersUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendRenewalRemindersService.class);

    /** Reminder intervals: days before due date → stage identifier. */
    private static final Map<Integer, String> REMINDER_INTERVALS = Map.of(
            7, "reminder_7d",
            3, "reminder_3d",
            1, "reminder_1d"
    );

    /** Notification type mapping: stage → notification type. */
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
    private final NotificationLogPort notificationLogPort;
    private final Clock clock;

    public SendRenewalRemindersService(
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            ProfileRepositoryPort profileRepositoryPort,
            AccountRepositoryPort accountRepositoryPort,
            ServiceRepositoryPort serviceRepositoryPort,
            NotificationLogPort notificationLogPort,
            Clock clock) {
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.profileRepositoryPort = profileRepositoryPort;
        this.accountRepositoryPort = accountRepositoryPort;
        this.serviceRepositoryPort = serviceRepositoryPort;
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
            String stage = entry.getValue();
            String notificationType = STAGE_TO_TYPE.get(stage);

            LocalDate targetDate = today.plusDays(daysAhead);
            List<Subscription> dueSubscriptions = subscriptionRepositoryPort.findActiveByPaymentDueDate(targetDate);

            for (Subscription sub : dueSubscriptions) {
                // Dedup check: skip if already notified for this stage
                if (notificationLogPort.existsByEntityAndStage("subscription", sub.getId(), stage)) {
                    continue;
                }

                try {
                    String payload = buildPayload(sub, daysAhead);
                    String recipientEmail = resolveClientEmail(sub);

                    if (recipientEmail == null) {
                        log.warn("Cannot send reminder for subscription {}: client email not found", sub.getUuid());
                        continue;
                    }

                    notificationLogPort.record(notificationType, recipientEmail, payload,
                            "subscription", sub.getId(), stage);
                    totalSent++;
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
