package com.neversion.api.vendor.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model representing a vendor (business) operating within the platform.
 * Each vendor manages its own catalog, clients, and inventory in isolation (ADR-02).
 * <p>
 * bankDetails and discountCfg are stored as raw JSON strings — the domain treats
 * them as opaque blobs; parsing is the responsibility of application services.
 * <p>
 * discountCfg is mutable to allow in-place updates from the vendor panel (BR-13 v2).
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
     * Structure: { "min_items": 2, "max_items": 4, "round_to": 5, "tiers": [{ "count": 2, "discount_pct": 25 }] }
     * Mutable — updated via the vendor panel discount configuration endpoint.
     */
    @Setter
    private String discountCfg;

    /**
     * Rewards/loyalty points configuration as JSON.
     * Structure: { "enabled": true, "earn_pct": 2.0 }
     * Mutable — updated via the vendor panel rewards configuration endpoint.
     */
    @Setter
    private String rewardsCfg;

    private final Instant createdAt;
}
