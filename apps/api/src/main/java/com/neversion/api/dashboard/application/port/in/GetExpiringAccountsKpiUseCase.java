package com.neversion.api.dashboard.application.port.in;

import com.neversion.api.dashboard.application.result.ExpiringAccountsKpiResult;

public interface GetExpiringAccountsKpiUseCase {

    /**
     * Returns the authenticated vendor's master accounts due for renewal
     * today, tomorrow and later this week.
     */
    ExpiringAccountsKpiResult getForAuthenticatedVendor(String callerExternalId);
}
