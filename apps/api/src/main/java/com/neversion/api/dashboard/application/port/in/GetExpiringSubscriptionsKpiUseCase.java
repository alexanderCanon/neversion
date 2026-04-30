package com.neversion.api.dashboard.application.port.in;

import com.neversion.api.dashboard.application.result.ExpiringSubscriptionsKpiResult;

public interface GetExpiringSubscriptionsKpiUseCase {

    /**
     * US-063: Returns the authenticated vendor's subscriptions due today,
     * tomorrow and later this week.
     */
    ExpiringSubscriptionsKpiResult getForAuthenticatedVendor(String callerExternalId);
}
