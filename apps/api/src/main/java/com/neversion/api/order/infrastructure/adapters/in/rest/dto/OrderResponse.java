package com.neversion.api.order.infrastructure.adapters.in.rest.dto;

import java.time.Instant;
import java.util.UUID;

import com.neversion.api.order.domain.model.enums.OrderStatus;

import lombok.Builder;

@Builder
public record OrderResponse(
        UUID id,
        UUID reservationId,
        OrderStatus status,
        String notes,
        Instant createdAt) {
}
