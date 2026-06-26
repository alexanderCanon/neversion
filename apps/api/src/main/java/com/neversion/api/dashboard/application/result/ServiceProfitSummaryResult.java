package com.neversion.api.dashboard.application.result;

import java.math.BigDecimal;

/**
 * Per-service profit summary aggregated from account-level margins.
 */
public record ServiceProfitSummaryResult(
        String serviceName,
        int activeAccounts,
        int totalProfilesSold,
        BigDecimal totalRevenue,
        BigDecimal totalAllocatedCost,
        BigDecimal totalProfit,
        BigDecimal avgMarginPct) {
}
