package com.neversion.api.profile.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Audit record that captures a snapshot of the profile slot and master account
 * credentials at the moment a subscription is activated.
 *
 * Released when the subscription is cancelled; for Spotify BY_PROFILE slots
 * this also triggers a profile reset (name → "Perfil", pin/notes → null).
 */
@Getter
@Setter
@Builder
public class ProfileAssignmentHistory {

    /** Internal DB PK. */
    private Long id;

    /** External identifier. */
    private UUID uuid;

    /** FK to profiles.id — the slot that was assigned. */
    private Long profileId;

    /** FK to subscriptions.id — the subscription this entry belongs to. */
    private Long subscriptionId;

    // ── Snapshot of master account credentials ─────────────────────────────
    private String accountEmail;
    private String accountPassword;

    // ── Snapshot of profile slot data ──────────────────────────────────────
    private String profileName;
    private String profilePin;
    private String profileNotes;

    // ── Lifecycle ──────────────────────────────────────────────────────────

    /** Timestamp when the assignment became active. */
    private LocalDateTime assignedAt;

    /**
     * Timestamp when the slot was released (subscription cancelled).
     * Null while the assignment is still active.
     */
    private LocalDateTime releasedAt;

    /** FK to vendors.id — multi-tenancy isolation. */
    private Long vendorId;
}
