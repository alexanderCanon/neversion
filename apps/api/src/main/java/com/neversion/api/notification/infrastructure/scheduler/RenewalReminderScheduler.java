package com.neversion.api.notification.infrastructure.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.neversion.api.notification.application.port.in.SendAccountRenewalRemindersUseCase;
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
    private final SendAccountRenewalRemindersUseCase sendAccountRenewalRemindersUseCase;

    public RenewalReminderScheduler(
            SendRenewalRemindersUseCase sendRenewalRemindersUseCase,
            SendAccountRenewalRemindersUseCase sendAccountRenewalRemindersUseCase) {
        this.sendRenewalRemindersUseCase = sendRenewalRemindersUseCase;
        this.sendAccountRenewalRemindersUseCase = sendAccountRenewalRemindersUseCase;
    }

    @Scheduled(cron = "${neversion.cron.renewal-reminders.cron:0 0 8 * * *}")
    public void sendReminders() {
        int subscriptionCount = sendRenewalRemindersUseCase.sendReminders();
        int accountCount = sendAccountRenewalRemindersUseCase.sendReminders();
        int totalCount = subscriptionCount + accountCount;
        if (totalCount > 0) {
            log.info("Renewal reminder scheduler recorded {} reminders: subscriptions={}, accounts={}",
                    totalCount, subscriptionCount, accountCount);
        }
    }
}
