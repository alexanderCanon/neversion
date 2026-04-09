package com.neversion.api.profile.infrastructure.adapters.out;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA Entity for the 'profiles' table.
 * Represents a physical sub-division (profile/profile) of a master Account.
 * Formerly "ProfileEntity" — renamed to align with new domain language.
 *
 * 'id' (Long) is the internal PK used for DB relations.
 * 'uuid' (UUID) is the external identifier exposed to the frontend.
 * 'isOwner' = true indicates this profile has admin privileges inside the platform.
 */
@Entity
@Table(name = "profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileEntity {

    /** Internal auto-increment PK — never exposed to the frontend. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /** External UUID — used in all REST responses and frontend routes. */
    @Column(name = "uuid", updatable = false, nullable = false,
            columnDefinition = "uuid DEFAULT gen_random_uuid()")
    private UUID uuid;

    /** FK to accounts.id — the parent master credential this profile belongs to. */
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    /** Screen name configured in the streaming platform (e.g. "Victor R"). */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** Security PIN assigned to this profile in the streaming platform. */
    @Column(name = "pin", length = 20)
    private String pin;

    /**
     * True if this profile holds admin/owner privileges within the streaming account.
     * Only one profile per account should have isOwner = true.
     */
    @Column(name = "is_owner", nullable = false)
    private Boolean isOwner;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
