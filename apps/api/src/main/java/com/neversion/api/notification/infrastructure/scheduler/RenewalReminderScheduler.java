package com.neversion.api.notification.infrastructure.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.neversion.api.notification.application.port.in.SendRenewalRemindersUseCase;

/**
 * EPIC-08 US-054: Daily scheduler for renewal reminder notifications.
 * Runs daily at 8:00 AM (server timezone).
 */
@Component
@ConditionalOnProperty(name = "neversion.cron.renewal-reminders.enabled", havingValue = "true", matchIfMissing = false)
public class RenewalReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(RenewalReminderScheduler.class);

    private final SendRenewalRemindersUseCase sendRenewalRemindersUseCase;

    public RenewalReminderScheduler(SendRenewalRemindersUseCase sendRenewalRemindersUseCase) {
        this.sendRenewalRemindersUseCase = sendRenewalRemindersUseCase;
    }

    @Scheduled(cron = "${neversion.cron.renewal-reminders.cron:0 0 8 * * *}")
    public void sendReminders() {
        int count = sendRenewalRemindersUseCase.sendReminders();
        if (count > 0) {
            log.info("Renewal reminder scheduler sent {} reminders", count);
        }
    }
}
