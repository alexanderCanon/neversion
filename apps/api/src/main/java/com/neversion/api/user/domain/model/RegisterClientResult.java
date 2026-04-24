package com.neversion.api.user.domain.model;

import java.util.UUID;

/**
 * Result returned after successfully registering a client (US-013).
 * <p>
 * Only public identifiers (UUIDs) are exposed — internal BIGINT IDs
 * are never returned (NFR-01).
 *
 * @param userUuid          UUID of the created platform user.
 * @param clientUuid        UUID of the created client record.
 * @param name              Client's display name.
 * @param email             Client's email address.
 * @param temporaryPassword System-generated one-time password.
 */
public record RegisterClientResult(
        UUID userUuid,
        UUID clientUuid,
        String name,
        String email,
        String temporaryPassword
) {
}
