package com.neversion.api.service.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Domain model for a digital service offered by Neversion (e.g. Netflix, Spotify).
 * Replaces the legacy dual Product + Inventory model.
 *
 * 'id' (Long)  – internal identifier, used only for DB relations. Never exposed externally.
 * 'uuid' (UUID) – external identifier exposed in all REST responses and frontend routes.
 * 'details' (JsonNode) – JSONB payload with inventory metadata (pricing tiers, currencies, etc.)
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

    /**
     * Default number of profiles per account for this service.
     * Acts as the ceiling for profile creation (BR-01).
     */
    private Integer maxProfiles;

    /**
     * JSONB inventory metadata.
     * Example: {"pricing": [{"duration_days": 30, "price": 50.00, "currency": "GTQ"}]}
     */
    private JsonNode details;

    private LocalDateTime createdAt;

    public Service() {
    }

    public Service(Long id, UUID uuid, String name, Integer maxProfiles,
            JsonNode details, LocalDateTime createdAt) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.maxProfiles = maxProfiles;
        this.details = details;
        this.createdAt = createdAt;
    }
}
