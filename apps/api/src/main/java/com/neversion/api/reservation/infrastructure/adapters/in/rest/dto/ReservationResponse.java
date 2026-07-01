package com.neversion.api.reservation.infrastructure.adapters.in.rest.dto;

import com.neversion.api.reservation.domain.model.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.neversion.api.shared.domain.model.enums.AccountPreference;

/**
 * Response body for reservation operations.
 * 'id' maps to the reservation's public UUID — internal BIGINT IDs are never exposed (NFR-01).
 * EPIC-05: Added paymentMethod.
 */
public record ReservationResponse(
        UUID id,
        UUID clientId,
        /** Display name of the client — populated from Client domain for vendor visibility. */
        String clientName,
        /** Email of the client — populated from Client domain for vendor visibility. */
        String clientEmail,
        ReservationStatus status,
        BigDecimal discount,
        BigDecimal total,
        String receiptUrl,
        String paymentMethod,
        AccountPreference accountPreference,
        Instant expirationDate,
        Instant createdAt,
        UUID renewalSubscriptionId,
        List<ReservationDetailResponse> details,
        Long pointsRedeemed,
        BigDecimal pointsDiscount) {
}
