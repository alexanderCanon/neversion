package com.neversion.api.shared.port.out;

import java.util.List;

/**
 * Outbound port — contract for recording and managing notification events.
 * <p>
 * The backend inserts records (NFR-05).
 * NotificationWorker processes pending records and dispatches emails.
 */
public interface NotificationLogPort {

    /**
     * Records a notification event (backward-compatible, no entity tracking).
     *
     * @param type           notification type (e.g. "VENDOR_WELCOME")
     * @param recipientEmail destination email address
     * @param payload        JSON string with template variables
     */
    void record(String type, String recipientEmail, String payload);

    /**
     * Records a notification event with entity tracking for dedup (EPIC-08).
     *
     * @param type           notification type
     * @param recipientEmail destination email address
     * @param payload        JSON string with template variables
     * @param entityType     entity category: "client", "order", "subscription", "vendor"
     * @param entityId       internal entity ID
     * @param stage          lifecycle stage: "welcome", "approved", "reminder_7d", etc.
     */
    void record(String type, String recipientEmail, String payload,
                String entityType, Long entityId, String stage);

    /**
     * Checks if a notification with the given entity+stage already exists (dedup for US-054).
     */
    boolean existsByEntityAndStage(String entityType, Long entityId, String stage);

    /**
     * Retrieves pending notifications for the worker to process.
     *
     * @param limit max records to return per batch
     * @return list of pending notification records
     */
    List<PendingNotification> findPending(int limit);

    /**
     * Marks a notification as successfully sent.
     */
    void markSent(Long id);

    /**
     * Marks a notification as failed with an error message.
     */
    void markFailed(Long id, String errorMessage);

    /**
     * DTO for pending notifications — avoids exposing entity to domain.
     */
    record PendingNotification(
            Long id,
            String type,
            String recipientEmail,
            String payload) {
    }
}
