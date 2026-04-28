package com.neversion.api.reservation.infrastructure.adapters.in.rest.dto;

import com.neversion.api.reservation.domain.model.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response body for reservation operations.
 * 'id' maps to the reservation's public UUID — internal BIGINT IDs are never exposed (NFR-01).
 * EPIC-05: Added paymentMethod.
 */
public record ReservationResponse(
        UUID id,
        UUID clientId,
        ReservationStatus status,
        BigDecimal discount,
        BigDecimal total,
        String receiptUrl,
        String paymentMethod,
        Instant expirationDate,
        Instant createdAt,
        List<ReservationDetailResponse> details) {
}
