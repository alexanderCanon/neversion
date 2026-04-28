package com.neversion.api.order.infrastructure.adapters.in.rest.dto;

import com.neversion.api.order.domain.model.enums.OrderStatus;

import java.time.Instant;

/**
 * US-038 CA3: Status change record for chronological history.
 */
public record StatusChangeResponse(
        OrderStatus oldStatus,
        OrderStatus newStatus,
        String changedBy,
        String notes,
        Instant changedAt) {
}
