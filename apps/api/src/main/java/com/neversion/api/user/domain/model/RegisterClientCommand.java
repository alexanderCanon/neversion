package com.neversion.api.user.domain.model;

import java.util.UUID;

/**
 * Command object for client self-registration (US-013).
 * <p>
 * Passed from the auth REST adapter to the RegisterClientUseCase.
 * Pure Java record — no framework dependencies.
 *
 * @param email      Client's email address (used for Supabase Auth and notifications).
 * @param name       Client's display name.
 * @param phone      Optional phone number — primary WhatsApp contact channel.
 * @param vendorUuid UUID of the vendor (store) the client is registering with (ADR-02).
 *
 * NOTE (ADR-09): The corresponding Supabase Auth account must be created
 * manually or via the Supabase dashboard. The backend generates a temporary
 * password and records it in notification_log for the confirmation email.
 */
public record RegisterClientCommand(
        String email,
        String name,
        String phone,
        UUID vendorUuid
) {
}
