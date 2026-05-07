package com.neversion.api.user.domain.model;

/**
 * Command object carrying all data required to register a new vendor (US-012).
 * <p>
 * The Supabase Auth account is created by the backend.
 *
 * @param email       Vendor's email address (for notifications and Supabase Auth).
 * @param password    Vendor's chosen password.
 * @param storeName   Display name for the vendor's storefront.
 * @param logoUrl     Optional URL to the vendor's logo.
 * @param bankDetails Optional JSON with bank/payment details.
 * @param discountCfg Optional JSON with discount tier configuration (BR-13).
 */
public record RegisterVendorCommand(
        String email,
        String password,
        String storeName,
        String logoUrl,
        String bankDetails,
        String discountCfg
) {
}
