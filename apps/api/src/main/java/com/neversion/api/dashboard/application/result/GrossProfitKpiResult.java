package com.neversion.api.dashboard.application.result;

import java.math.BigDecimal;

/**
 * US-067 KPI projection for current-period gross profit.
 */
public record GrossProfitKpiResult(BigDecimal grossProfit, String currency) {
}
