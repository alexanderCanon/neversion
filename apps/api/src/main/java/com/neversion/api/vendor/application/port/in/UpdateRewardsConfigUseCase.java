package com.neversion.api.vendor.application.port.in;

/**
 * Inbound port for updating a vendor's loyalty points (rewards) configuration.
 * <p>
 * The caller is identified by their Supabase external ID (JWT subject).
 */
public interface UpdateRewardsConfigUseCase {

    /**
     * Updates the rewards_cfg JSON for the vendor identified by the caller's external ID.
     *
     * @param callerExternalId Supabase subject (JWT {@code sub}) of the authenticated vendor
     * @param rewardsCfgJson   valid JSON string: { "enabled": true, "earn_pct": 2.0 }
     * @return the updated rewards_cfg JSON as persisted
     */
    String updateRewardsConfig(String callerExternalId, String rewardsCfgJson);
}
