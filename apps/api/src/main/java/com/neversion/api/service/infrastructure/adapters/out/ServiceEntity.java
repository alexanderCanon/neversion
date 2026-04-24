package com.neversion.api.service.infrastructure.adapters.out;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.neversion.api.shared.domain.model.enums.CategoryType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * JPA Entity for the 'services' table.
 * Replaces the legacy ProductEntity + InventoryEntity dual-table approach.
 * 'id' (Long) is the internal PK used for DB relations.
 * 'uuid' (UUID) is the external identifier exposed to the frontend.
 * 'details' (JSONB) stores inventory-like metadata (pricing, duration options, etc.)
 */
@Entity
@Table(name = "services")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceEntity {

    /** Internal auto-increment PK — never exposed to the frontend. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /** External UUID — the identifier used in all REST responses and frontend routes. */
    @Column(name = "uuid", updatable = false, nullable = false,
            columnDefinition = "uuid DEFAULT gen_random_uuid()")
    private UUID uuid;

    /** Human-readable platform name, e.g. "Netflix", "Spotify Family". */
    @Column(name = "name", nullable = false, unique = true, length = 150)
    private String name;

    /**
     * Default number of profiles supported per account for this service.
     * Governs how many profiles can be created per account (BR-01).
     */
    @Column(name = "max_profiles")
    private Integer maxProfiles;

    /**
     * Free-form JSONB column for inventory metadata.
     * Example: {"pricing": [{"duration_days": 30, "price": 50.00}], "currency": "GTQ"}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "jsonb")
    private JsonNode details;

    /**
     * Service category — used by the Dashboard to filter products by type.
     * Values: STREAMING, SOFTWARE, GIFT_CARD, RECHARGE, DIGITAL_SERVICE.
     * Added in V5. Stored as lowercase varchar via the EnumConverter.
     */
    @Column(name = "category", nullable = false, length = 50)
    private CategoryType category;

    /** FK to vendors.id — multi-tenancy catalog isolation (ADR-02, US-004). DB FK by V10. */
    @Column(name = "vendor_id")
    private Long vendorId;

    /** Human-readable description (US-005). */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** URL to the service image/logo (US-005). */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /** Price per individual profile sale (US-005). */
    @Column(name = "price_profile", precision = 10, scale = 2)
    private java.math.BigDecimal priceProfile;

    /** Price for full account sale (US-005). */
    @Column(name = "price_full", precision = 10, scale = 2)
    private java.math.BigDecimal priceFull;

    /** Subscription duration in days (US-005). */
    @Column(name = "duration_days")
    private Integer durationDays;

    /** Whether this service is currently available for sale (US-005). */
    @Column(name = "is_active", nullable = false)
    @lombok.Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void prePersist() {
        if (uuid == null) uuid = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
