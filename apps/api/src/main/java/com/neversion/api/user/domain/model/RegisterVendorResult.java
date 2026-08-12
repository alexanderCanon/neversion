package com.neversion.api.user.domain.model;

import java.util.UUID;

/**
 * Result returned after successfully registering a vendor (US-012).
 * <p>
 * Only public identifiers (UUIDs) are exposed — internal BIGINT IDs
 * are never returned (NFR-01).
 *
 * @param userUuid   UUID of the created platform user.
 * @param vendorUuid UUID of the created vendor record.
 * @param storeName  Vendor's store display name.
 * @param email      Vendor's email address.
 */
public record RegisterVendorResult(
        String externalId,
        UUID vendorUuid,
        String storeName,
        String email
) {
}

