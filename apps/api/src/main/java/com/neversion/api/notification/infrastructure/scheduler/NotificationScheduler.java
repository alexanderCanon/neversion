package com.neversion.api.notification.infrastructure.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.neversion.api.notification.application.port.in.ProcessNotificationsUseCase;

/**
 * EPIC-08: Scheduled worker that processes pending notifications every 30 seconds.
 * Controlled by neversion.cron.notification-worker.enabled property.
 */
@Component
@EnableScheduling
@ConditionalOnProperty(name = "neversion.cron.notification-worker.enabled", havingValue = "true", matchIfMissing = false)
public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    private final ProcessNotificationsUseCase processNotificationsUseCase;

    public NotificationScheduler(ProcessNotificationsUseCase processNotificationsUseCase) {
        this.processNotificationsUseCase = processNotificationsUseCase;
    }

    @Scheduled(fixedDelayString = "${neversion.cron.notification-worker.interval-ms:30000}")
    public void processNotifications() {
        int count = processNotificationsUseCase.processNextBatch();
        if (count > 0) {
            log.info("Notification worker processed {} emails", count);
        }
    }
}
