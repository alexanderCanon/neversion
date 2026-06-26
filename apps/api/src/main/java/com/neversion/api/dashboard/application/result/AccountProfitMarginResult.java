package com.neversion.api.dashboard.application.result;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Per-account profit margin projection for a given period.
 * Includes revenue from new subscriptions and renewals, allocated cost,
 * and net profit. Accounts with zero sales in the period are included
 * to surface idle capacity costs.
 */
public record AccountProfitMarginResult(
        UUID accountUuid,
        String email,
        String serviceName,
        String saleMode,
        BigDecimal accountCost,
        int maxProfiles,
        int profilesSold,
        BigDecimal newRevenue,
        BigDecimal renewalRevenue,
        BigDecimal totalRevenue,
        BigDecimal totalDiscount,
        BigDecimal allocatedCost,
        BigDecimal profit,
        BigDecimal marginPct) {
}
