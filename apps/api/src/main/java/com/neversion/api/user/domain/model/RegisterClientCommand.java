package com.neversion.api.user.domain.model;

import java.util.UUID;

/**
 * Command object for client self-registration (US-013).
 * <p>
 * The Supabase Auth account is created by the frontend before calling this endpoint.
 * The {@code externalId} is the UUID already assigned by Supabase — persisted in
 * the local users table to maintain the 1:1 link (ADR-09 revised).
 *
 * @param email      Client's email address (used for Supabase Auth and notifications).
 * @param externalId Supabase Auth UUID for this client — provided by the frontend.
 * @param name       Client's display name.
 * @param phone      Optional phone number — primary WhatsApp contact channel.
 * @param vendorUuid UUID of the vendor (store) the client is registering with (ADR-02).
 */
public record RegisterClientCommand(
        String email,
        String externalId,
        String name,
        String phone,
        UUID vendorUuid
) {
}
