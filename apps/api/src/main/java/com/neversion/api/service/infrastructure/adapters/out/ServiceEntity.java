package com.neversion.api.service.infrastructure.adapters.out;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

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

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void prePersist() {
        if (uuid == null) uuid = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
