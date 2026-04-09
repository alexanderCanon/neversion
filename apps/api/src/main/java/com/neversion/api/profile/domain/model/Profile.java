package com.neversion.api.profile.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

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

    /** Screen name configured inside the streaming platform (e.g. "Victor R"). */
    private String name;

    /** Security PIN assigned to this profile within the platform. */
    private String pin;

    /**
     * Indicates if this profile holds admin/owner rights within the account.
     * Only one profile per account should have isOwner = true.
     */
    private Boolean isOwner;

    private LocalDateTime createdAt;

    public Profile(Long id, UUID uuid, Long accountId, String name, String pin,
            Boolean isOwner, LocalDateTime createdAt) {
        this.id = id;
        this.uuid = uuid;
        this.accountId = accountId;
        this.name = name;
        this.pin = pin;
        this.isOwner = isOwner;
        this.createdAt = createdAt;
    }
}
