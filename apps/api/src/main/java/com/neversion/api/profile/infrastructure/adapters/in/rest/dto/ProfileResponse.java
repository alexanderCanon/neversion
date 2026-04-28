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
        Boolean isOwner,
        ProfileStatus status,
        LocalDateTime createdAt) {
}
