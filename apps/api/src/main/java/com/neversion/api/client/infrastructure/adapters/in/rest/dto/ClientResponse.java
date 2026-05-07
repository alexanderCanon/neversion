package com.neversion.api.client.infrastructure.adapters.in.rest.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;

/**
 * Response payload for a client (end consumer).
 * US-029: includes activeSubscriptionCount for the vendor's list view.
 */
@Builder
public record ClientResponse(
        UUID id,
        String name,
        String email,
        String phone,
        String notes,
        long activeSubscriptionCount,
        LocalDateTime createdAt) {
}
