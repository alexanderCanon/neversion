package com.neversion.api.loyalty.infrastructure.adapters.in.rest.dto;

import java.time.Instant;
import java.util.UUID;

import com.neversion.api.loyalty.domain.model.enums.PointsEntryStatus;
import com.neversion.api.loyalty.domain.model.enums.PointsEntryType;

/**
 * Response body for a single points ledger movement.
 * 'id' maps to the entry's public UUID — internal BIGINT IDs are never exposed (NFR-01).
 * orderId/reservationId are omitted since the ledger only stores internal FKs (NFR-01).
 */
public record PointsMovementResponse(
        UUID id,
        PointsEntryType entryType,
        PointsEntryStatus status,
        long points,
        String notes,
        Instant createdAt) {
}
