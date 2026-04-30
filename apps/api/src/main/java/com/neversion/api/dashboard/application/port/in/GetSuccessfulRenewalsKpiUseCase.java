package com.neversion.api.dashboard.application.port.in;

import com.neversion.api.dashboard.application.result.SuccessfulRenewalsKpiResult;

public interface GetSuccessfulRenewalsKpiUseCase {

    /**
     * US-066: Returns the authenticated vendor's successful renewal count for the current month.
     */
    SuccessfulRenewalsKpiResult getForAuthenticatedVendor(String callerExternalId);
}
