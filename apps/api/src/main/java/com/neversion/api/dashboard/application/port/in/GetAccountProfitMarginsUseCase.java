package com.neversion.api.dashboard.application.port.in;

import com.neversion.api.dashboard.application.result.ProfitMarginsResult;

/**
 * Inbound port: get per-account profit margins for a given calendar month.
 * Includes per-service summaries and a grand total.
 */
public interface GetAccountProfitMarginsUseCase {

    /**
     * Returns profit margins for the specified year/month, or the current
     * calendar month if both parameters are null.
     *
     * @param callerExternalId the JWT subject of the authenticated vendor
     * @param year             nullable year (e.g. 2026)
     * @param month            nullable month (1-12)
     */
    ProfitMarginsResult getForAuthenticatedVendor(String callerExternalId, Integer year, Integer month);
}
