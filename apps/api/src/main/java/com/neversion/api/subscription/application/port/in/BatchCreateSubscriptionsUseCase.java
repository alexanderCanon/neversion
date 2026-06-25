package com.neversion.api.subscription.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BatchCreateSubscriptionsUseCase {

    /**
     * Creates multiple subscriptions for a single client in one operation.
     * Each item may specify a profileId (manual override) or null (auto-assign).
     * Partial success is possible: failed items are reported in the result.
     */
    BatchResult create(BatchCommand command, String callerExternalId);

    record BatchCommand(
            UUID clientUuid,
            List<BatchItemCommand> items,
            BigDecimal discountApplied,
            LocalDate paymentDueDate,
            String notes,
            boolean sendNotification) {
    }

    record BatchItemCommand(
            UUID serviceUuid,
            int quantity,
            BigDecimal priceSold,
            UUID profileUuid) {
    }

    record BatchResult(
            int totalRequested,
            int successCount,
            int failedCount,
            List<BatchItemResult> results) {
    }

    record BatchItemResult(
            UUID serviceUuid,
            boolean success,
            UUID subscriptionUuid,
            String errorMessage) {
    }
}
