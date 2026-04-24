package com.neversion.api.user.domain.model;

/**
 * Command object carrying all data required to register a new vendor.
 * <p>
 * Passed from the REST adapter to the application use case.
 * Pure Java record — no framework dependencies.
 *
 * @param email          Vendor's email address (used for Supabase Auth and notifications).
 * @param storeName      Display name for the vendor's storefront.
 * @param logoUrl        Optional URL to the vendor's logo.
 * @param bankDetails    Optional JSON with bank/payment details.
 * @param discountCfg    Optional JSON with discount tier configuration (BR-13).
 *
 * NOTE (ADR-09): The corresponding Supabase Auth account must be created
 * manually by the Super Admin using the Supabase dashboard or Admin API.
 * The backend will generate a temporary password and record it in
 * notification_log for the onboarding email.
 */
public record RegisterVendorCommand(
        String email,
        String storeName,
        String logoUrl,
        String bankDetails,
        String discountCfg
) {
}
