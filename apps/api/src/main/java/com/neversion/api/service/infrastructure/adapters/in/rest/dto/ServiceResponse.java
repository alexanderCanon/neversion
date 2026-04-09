package com.neversion.api.service.infrastructure.adapters.in.rest.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;

@Builder
public record ServiceResponse(
        UUID id,
        String name,
        Integer maxProfiles,
        String details,
        LocalDateTime createdAt) {
}
