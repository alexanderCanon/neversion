package com.neversion.api.order.infrastructure.adapters.in.rest.dto;

import com.neversion.api.order.domain.model.enums.OrderStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response body for order operations.
 * 'id' maps to the order's public UUID — internal BIGINT IDs are never exposed (NFR-01).
 * EPIC-05: Added paymentMethod, approvedAt, total.
 */
@Builder
public record OrderResponse(
        UUID id,
        UUID reservationId,
        OrderStatus status,
        String paymentMethod,
        BigDecimal total,
        BigDecimal discount,
        String notes,
        String receiptUrl,
        Instant approvedAt,
        Instant createdAt) {
}
