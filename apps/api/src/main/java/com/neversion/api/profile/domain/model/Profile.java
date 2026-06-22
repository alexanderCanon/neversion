package com.neversion.api.profile.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.neversion.api.profile.domain.model.enums.ProfileStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Domain model for a profile (sub-division) inside a master Account.
 * Formerly "Profile" — renamed to align with natural business language.
 *
 * 'id' (Long)  – internal identifier, used only for DB relations. Never exposed externally.
 * 'uuid' (UUID) – external identifier exposed in all REST responses and frontend routes.
 * 'isOwner' = true → this profile has admin privileges inside the streaming platform.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
public class Profile {

    /** Internal DB PK — used for JPA relations (subscriptions FK). */
    private Long id;

    /** External identifier — exposed to the frontend instead of the numeric id. */
    private UUID uuid;

    /** FK to Account — the master credential this profile belongs to. */
    private Long accountId;

    /**
     * Transient — carries the account UUID from the REST request to the service layer.
     * Resolved to accountId (Long) by ProfileService.save().
     * Never persisted.
     */
    private UUID accountUuid;

    /** Screen name configured inside the streaming platform (e.g. "Victor R"). */
    private String name;

    /** Security PIN assigned to this profile within the platform. */
    private String pin;

    /**
     * Operational notes for this profile slot.
     * Used in Spotify Family (BY_PROFILE) to store invitation links or the
     * client's personal email. Reset to null when the slot is released.
     */
    private String notes;

    /**
     * Indicates if this profile holds admin/owner rights within the account.
     * Only one profile per account should have isOwner = true.
     */
    private Boolean isOwner;

    /**
     * Operational status of this profile (US-022 / US-027).
     * Defaults to AVAILABLE on creation.
     */
    @Builder.Default
    private ProfileStatus status = ProfileStatus.AVAILABLE;

    /** FK to vendors.id — multi-tenancy isolation (ADR-02). */
    private Long vendorId;

    private LocalDateTime createdAt;

    public Profile(Long id, UUID uuid, Long accountId, UUID accountUuid, String name, String pin,
            String notes, Boolean isOwner, ProfileStatus status, Long vendorId, LocalDateTime createdAt) {
        this.id = id;
        this.uuid = uuid;
        this.accountId = accountId;
        this.accountUuid = accountUuid;
        this.name = name;
        this.pin = pin;
        this.notes = notes;
        this.isOwner = isOwner;
        this.status = status != null ? status : ProfileStatus.AVAILABLE;
        this.vendorId = vendorId;
        this.createdAt = createdAt;
    }
}
