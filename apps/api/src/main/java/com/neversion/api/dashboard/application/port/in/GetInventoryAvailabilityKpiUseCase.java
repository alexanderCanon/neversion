package com.neversion.api.dashboard.application.port.in;

import java.util.List;

import com.neversion.api.dashboard.application.result.InventoryAvailabilityResult;

public interface GetInventoryAvailabilityKpiUseCase {

    /**
     * US-064: Returns the authenticated vendor's inventory availability grouped by service.
     */
    List<InventoryAvailabilityResult> getForAuthenticatedVendor(String callerExternalId);
}
