package com.neversion.api.client.infrastructure.adapters.in.rest.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;

@Builder
public record ClientResponse(
        UUID id,
        String name,
        String email,
        String phone,
        String notes,
        LocalDateTime createdAt) {
}
