package com.neversion.api.shared.port.out;

/**
 * Outbound port — contract for recording transactional notification events.
 * <p>
 * The backend only inserts records here (NFR-05).
 * Agent Notifications is responsible for the actual email dispatch.
 */
public interface NotificationLogPort {

    /**
     * Records a notification event to be processed by Agent Notifications.
     *
     * @param type           notification type (e.g. "VENDOR_WELCOME")
     * @param recipientEmail destination email address
     * @param payload        JSON string with template variables
     */
    void record(String type, String recipientEmail, String payload);
}
