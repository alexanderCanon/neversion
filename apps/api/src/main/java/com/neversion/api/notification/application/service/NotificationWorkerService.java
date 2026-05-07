package com.neversion.api.notification.application.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.neversion.api.notification.application.port.in.ProcessNotificationsUseCase;
import com.neversion.api.notification.application.service.template.NotificationTemplateResolver;
import com.neversion.api.notification.application.service.template.NotificationTemplateResolver.TemplateSpec;
import com.neversion.api.shared.port.out.EmailSenderPort;
import com.neversion.api.shared.port.out.NotificationLogPort;
import com.neversion.api.shared.port.out.NotificationLogPort.PendingNotification;

/**
 * EPIC-08: Notification Worker — processes pending notification_log records.
 * Reads pending → renders template → sends email via EmailSenderPort → marks sent/failed.
 */
@Service
public class NotificationWorkerService implements ProcessNotificationsUseCase {

    private static final Logger log = LoggerFactory.getLogger(NotificationWorkerService.class);
    private static final int DEFAULT_BATCH_SIZE = 50;

    private final NotificationLogPort notificationLogPort;
    private final EmailSenderPort emailSenderPort;
    private final NotificationTemplateResolver templateResolver;

    public NotificationWorkerService(
            NotificationLogPort notificationLogPort,
            EmailSenderPort emailSenderPort,
            NotificationTemplateResolver templateResolver) {
        this.notificationLogPort = notificationLogPort;
        this.emailSenderPort = emailSenderPort;
        this.templateResolver = templateResolver;
    }

    @Override
    public int processNextBatch() {
        return processNextBatch(DEFAULT_BATCH_SIZE);
    }

    @Override
    public int processNextBatch(int batchSize) {
        List<PendingNotification> pending = notificationLogPort.findPending(batchSize);

        if (pending.isEmpty()) {
            return 0;
        }

        log.info("Processing {} pending notifications", pending.size());
        int processed = 0;

        for (PendingNotification notification : pending) {
            try {
                TemplateSpec spec = templateResolver.resolve(notification.type(), notification.payload());

                if (spec.skipped()) {
                    // Internal-only notifications (e.g. vendor alerts) — mark sent without emailing
                    notificationLogPort.markSent(notification.id());
                    processed++;
                    continue;
                }

                emailSenderPort.send(notification.recipientEmail(), spec.subject(), spec.htmlBody());
                notificationLogPort.markSent(notification.id());
                processed++;

            } catch (Exception e) {
                log.error("Failed to process notification id={}, type={}: {}",
                        notification.id(), notification.type(), e.getMessage());
                notificationLogPort.markFailed(notification.id(),
                        e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 500)) : "Unknown error");
            }
        }

        log.info("Processed {}/{} notifications", processed, pending.size());
        return processed;
    }
}
