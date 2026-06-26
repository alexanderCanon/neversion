package com.neversion.api.subscription.infrastructure.adapters.in.rest.dto;

import java.util.List;
import java.util.UUID;

import lombok.Builder;

@Builder
public record BatchCreateSubscriptionsResponse(
        int totalRequested,
        int successCount,
        int failedCount,
        List<BatchItemResult> results) {

    public enum BatchItemStatus {
        SUCCESS,
        FAILED
    }

    @Builder
    public record BatchItemResult(
            UUID serviceId,
            BatchItemStatus status,
            UUID subscriptionId,
            String errorMessage) {
    }
}
