package com.neversion.api.dashboard.application.port.in;

import com.neversion.api.dashboard.application.result.GrossProfitKpiResult;

public interface GetGrossProfitKpiUseCase {

    /**
     * US-067: Returns the authenticated vendor's gross profit for the current month.
     */
    GrossProfitKpiResult getForAuthenticatedVendor(String callerExternalId);
}
