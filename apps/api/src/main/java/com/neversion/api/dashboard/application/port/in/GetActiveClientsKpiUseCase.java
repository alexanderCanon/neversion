package com.neversion.api.dashboard.application.port.in;

import com.neversion.api.dashboard.application.result.ActiveClientsKpiResult;

public interface GetActiveClientsKpiUseCase {

    /**
     * US-065: Returns the authenticated vendor's unique clients with at least one ACTIVE subscription.
     */
    ActiveClientsKpiResult getForAuthenticatedVendor(String callerExternalId);
}
