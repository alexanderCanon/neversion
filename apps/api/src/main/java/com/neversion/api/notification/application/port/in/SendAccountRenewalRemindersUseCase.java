package com.neversion.api.notification.application.port.in;

/**
 * Sends operational renewal reminders for master accounts.
 */
public interface SendAccountRenewalRemindersUseCase {

    /**
     * Scans master accounts by renewal date and records pending notification events.
     *
     * @return total number of reminders recorded
     */
    int sendReminders();
}
