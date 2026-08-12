package com.neversion.api.user.domain.model;

import com.neversion.api.user.domain.model.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Domain model representing an internal platform user.
 * Links external auth identity (Supabase Auth) to an internal role.
 * <p>
 * Pure Java — no Spring or JPA dependencies (hexagonal architecture).
 */
@Getter
@Builder
public class User {

    /** Internal surrogate key — used for database foreign keys. */
    private final Long id;

    /**
     * Subject claim (sub) from the external auth provider JWT (ADR-06 / ADR-09).
     * Used to link the authenticated identity to this platform user.
     */
    private final String externalId;

    /**
     * Platform role — drives RBAC guards in the backend (ADR-08).
     */
    private final UserRole role;

    private final Instant createdAt;
}

