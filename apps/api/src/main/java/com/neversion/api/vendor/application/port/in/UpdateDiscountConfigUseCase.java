package com.neversion.api.vendor.application.port.in;

/**
 * Inbound port for updating a vendor's discount configuration (BR-13 v2).
 * <p>
 * The caller is identified by their Supabase external ID (JWT subject).
 */
public interface UpdateDiscountConfigUseCase {

    /**
     * Updates the discount_cfg JSON for the vendor identified by the caller's external ID.
     *
     * @param callerExternalId Supabase subject (JWT {@code sub}) of the authenticated vendor
     * @param discountCfgJson  valid JSON string conforming to the BR-13 structure
     * @return the updated discount_cfg JSON as persisted
     */
    String updateDiscountConfig(String callerExternalId, String discountCfgJson);
}
