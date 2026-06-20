package com.neversion.api.user.domain.model;

import java.util.UUID;

/**
 * Command object for client self-registration (US-013).
 * <p>
 * The Supabase Auth account is created by the backend directly.
 *
 * @param email      Client's email address (used for Supabase Auth and notifications).
 * @param password   Client's chosen password.
 * @param name       Client's display name.
 * @param phone      Required phone number — links existing manual clients within a vendor.
 * @param vendorUuid UUID of the vendor (store) the client is registering with (ADR-02).
 */
public record RegisterClientCommand(
        String email,
        String password,
        String name,
        String phone,
        UUID vendorUuid,
        String externalId
) {
}
