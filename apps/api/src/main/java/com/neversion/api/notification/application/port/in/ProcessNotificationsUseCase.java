package com.neversion.api.notification.application.port.in;

/**
 * EPIC-08: Use case for processing pending notification records.
 */
public interface ProcessNotificationsUseCase {

    /**
     * Processes the next batch of pending notifications (default batch size).
     *
     * @return number of notifications processed
     */
    int processNextBatch();

    /**
     * Processes the next batch of pending notifications.
     *
     * @param batchSize max notifications to process
     * @return number of notifications processed
     */
    int processNextBatch(int batchSize);
}
