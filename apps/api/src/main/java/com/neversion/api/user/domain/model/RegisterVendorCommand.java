package com.neversion.api.user.domain.model;

/**
 * Command object carrying all data required to register a new vendor (US-012).
 * <p>
 * The Supabase Auth account is created by the frontend before calling this endpoint.
 * The {@code externalId} is the UUID already assigned by Supabase — persisted in
 * the local users table to maintain the 1:1 link (ADR-09 revised).
 *
 * @param email       Vendor's email address (for notifications and Supabase Auth).
 * @param externalId  Supabase Auth UUID for this vendor — provided by the frontend.
 * @param storeName   Display name for the vendor's storefront.
 * @param logoUrl     Optional URL to the vendor's logo.
 * @param bankDetails Optional JSON with bank/payment details.
 * @param discountCfg Optional JSON with discount tier configuration (BR-13).
 */
public record RegisterVendorCommand(
        String email,
        String externalId,
        String storeName,
        String logoUrl,
        String bankDetails,
        String discountCfg
) {
}
