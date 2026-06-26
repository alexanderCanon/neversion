package com.neversion.api.dashboard.application.result;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Aggregate result for the account profit margins endpoint.
 * Contains per-service summaries, per-account breakdowns, and a grand total.
 */
public record ProfitMarginsResult(
        LocalDate periodStart,
        LocalDate periodEnd,
        String currency,
        List<ServiceProfitSummaryResult> serviceSummaries,
        List<AccountProfitMarginResult> accountMargins,
        GrandTotal grandTotal) {

    public record GrandTotal(
            BigDecimal totalRevenue,
            BigDecimal totalAllocatedCost,
            BigDecimal totalProfit,
            BigDecimal avgMarginPct) {
    }
}
