package com.neversion.api.client.infrastructure.adapters.in.rest.dto;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.time.Instant;

/**
 * REST response for client detail (US-030).
 * Embeds ClientResponse (UUID-based id) instead of the raw domain Client,
 * ensuring the numeric internal PK is never exposed externally.
 */
public record ClientDetailResponse(
        ClientResponse client,
        List<ActiveSubscriptionSummaryDto> activeSubscriptions,
        List<OrderSummaryDto> orderHistory) {

    public record ActiveSubscriptionSummaryDto(
            UUID id,
            String serviceName,
            String profileName,
            LocalDate paymentDueDate,
            String status) {}

    public record OrderSummaryDto(
            UUID id,
            String status,
            Instant createdAt) {}
}
