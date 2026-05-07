package com.neversion.api.notification.application.port.in;

/**
 * EPIC-08 US-054: Use case for sending renewal reminder notifications (7d, 3d, 1d).
 */
public interface SendRenewalRemindersUseCase {

    /**
     * Scans active subscriptions for upcoming due dates and records reminder
     * notifications for those not yet notified.
     *
     * @return total number of reminders recorded
     */
    int sendReminders();
}
