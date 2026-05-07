package com.neversion.api.dashboard.application.result;

import java.util.UUID;

/**
 * US-064 KPI projection for sellable inventory capacity grouped by service.
 */
public record InventoryAvailabilityResult(
        UUID serviceId,
        String serviceName,
        long availableProfiles,
        long occupiedProfiles,
        long availableFullAccounts,
        long occupiedFullAccounts) {
}
