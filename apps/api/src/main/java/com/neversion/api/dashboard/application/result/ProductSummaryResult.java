package com.neversion.api.dashboard.application.result;

import java.util.UUID;

/**
 * Read-only projection for endpoint 1: product summary with account count.
 */
public record ProductSummaryResult(
        UUID productId,
        String productName,
        String category,
        int totalAccounts) {
}
