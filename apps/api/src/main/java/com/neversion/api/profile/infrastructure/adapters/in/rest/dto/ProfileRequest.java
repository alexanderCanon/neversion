package com.neversion.api.profile.infrastructure.adapters.in.rest.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Request body for creating or editing a profile (US-022 / US-026).
 * accountId: UUID (external identifier — backend resolves to internal Long).
 */
@Builder
public record ProfileRequest(
        @NotNull(message = "Account ID is required") UUID accountId,

        @Size(max = 100, message = "Name must not exceed 100 characters") String name,

        @Size(max = 20, message = "Pin must not exceed 20 characters") String pin,

        /** Invitation link or personal email for Spotify Family slots. */
        String notes,

        Boolean isOwner) {
}
