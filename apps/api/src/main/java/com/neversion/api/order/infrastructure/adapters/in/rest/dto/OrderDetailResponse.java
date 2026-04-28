package com.neversion.api.order.infrastructure.adapters.in.rest.dto;

import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.reservation.infrastructure.adapters.in.rest.dto.ReservationResponse;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * US-038: Full order detail including reservation origin data and status history.
 */
@Builder
public record OrderDetailResponse(
        UUID id,
        UUID reservationId,
        OrderStatus status,
        String paymentMethod,
        BigDecimal total,
        BigDecimal discount,
        String notes,
        String receiptUrl,
        Instant approvedAt,
        Instant createdAt,
        ReservationResponse reservation,
        List<StatusChangeResponse> statusHistory) {
}
