package com.neversion.api.vendor.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model representing a vendor (business) operating within the platform.
 * Each vendor manages its own catalog, clients, and inventory in isolation (ADR-02).
 * <p>
 * bankDetails and discountCfg are stored as raw JSON strings — the domain treats
 * them as opaque blobs; parsing is the responsibility of application services.
 * <p>
 * Pure Java — no Spring or JPA dependencies.
 */
@Getter
@Builder
public class Vendor {

    /** Internal surrogate key — never exposed in API responses (NFR-01). */
    private final Long id;

    /** Public identifier — the only ID exposed through the API (NFR-01). */
    private final UUID uuid;

    /**
     * FK to users.id — the authenticated user who owns this vendor account.
     * Role must be 'vendor' (ADR-08).
     */
    private final Long userId;

    /** Display name shown in the vendor's storefront. */
    private final String storeName;

    /** URL to the vendor's logo image. */
    private final String logoUrl;

    /**
     * Bank / payment details as JSON (e.g., account number, bank name).
     * Stored opaque — parsed only at the application layer when needed.
     */
    private final String bankDetails;

    /**
     * Discount tier configuration as JSON (BR-13).
     * Structure: { "min_items": 2, "tiers": [{ "from": 2, "to": 3, "discount_pct": 5 }] }
     */
    private final String discountCfg;

    private final Instant createdAt;
}
