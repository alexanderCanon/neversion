package com.neversion.api.service.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.neversion.api.shared.domain.model.enums.CategoryType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Domain model for a digital service offered by Neversion (e.g. Netflix,
 * Spotify).
 * Replaces the legacy dual Product + Inventory model.
 *
 * 'id' (Long) – internal identifier, used only for DB relations. Never exposed
 * externally.
 * 'uuid' (UUID) – external identifier exposed in all REST responses and
 * frontend routes.
 * 'details' (JsonNode) – JSONB payload with inventory metadata (pricing tiers,
 * currencies, etc.)
 */
@Getter
@Setter
@Builder
public class Service {

    /** Internal DB PK — used for JPA relations (accounts FK). */
    private Long id;

    /** External identifier — exposed to the frontend instead of the numeric id. */
    private UUID uuid;

    /** Human-readable platform name, e.g. "Netflix", "Spotify Family". */
    private String name;

    /** FK to vendors.id — multi-tenancy catalog isolation (ADR-02, US-004). */
    private Long vendorId;

    /**
     * Default number of profiles per account for this service.
     * Acts as the ceiling for profile creation (BR-01).
     */
    private Integer maxProfiles;

    /**
     * JSONB inventory metadata.
     * Example: {"pricing": [{"duration_days": 30, "price": 50.00, "currency":
     * "GTQ"}]}
     */
    private JsonNode details;

    /**
     * Service category — used by the Dashboard for filtering.
     * Values: STREAMING, SOFTWARE, GIFT_CARD, RECHARGE, DIGITAL_SERVICE.
     */
    private CategoryType category;

    /** Human-readable description of the service (US-005). */
    private String description;

    /** URL to the service image/logo (US-005). */
    private String imageUrl;

    /** Price per individual profile sale (US-005). */
    private java.math.BigDecimal priceProfile;

    /** Price for full account sale (US-005). */
    private java.math.BigDecimal priceFull;

    /** Subscription duration in days for this service (US-005). */
    private Integer durationDays;

    /** Whether this service is currently available for sale (US-005). */
    @lombok.Builder.Default
    private Boolean isActive = true;

    private LocalDateTime createdAt;

    public Service() {
    }

    public Service(Long id, UUID uuid, String name, Long vendorId, Integer maxProfiles,
            JsonNode details, CategoryType category, String description, String imageUrl,
            java.math.BigDecimal priceProfile, java.math.BigDecimal priceFull,
            Integer durationDays, Boolean isActive, LocalDateTime createdAt) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.vendorId = vendorId;
        this.maxProfiles = maxProfiles;
        this.details = details;
        this.category = category;
        this.description = description;
        this.imageUrl = imageUrl;
        this.priceProfile = priceProfile;
        this.priceFull = priceFull;
        this.durationDays = durationDays;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }
}
