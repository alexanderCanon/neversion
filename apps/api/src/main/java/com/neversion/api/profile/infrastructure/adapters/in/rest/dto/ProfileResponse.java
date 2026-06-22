package com.neversion.api.profile.infrastructure.adapters.in.rest.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.neversion.api.profile.domain.model.enums.ProfileStatus;

import lombok.Builder;

@Builder
public record ProfileResponse(
        UUID id,
        Long accountId,
        String name,
        String pin,
        /** Operational notes: invitation link or personal email for Spotify Family. */
        String notes,
        Boolean isOwner,
        ProfileStatus status,
        LocalDateTime createdAt) {
}
