package com.neversion.api.reservation.infrastructure.adapters.in.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response for a single reservation line item.
 * 'id' maps to the detail's public UUID — internal BIGINT IDs are never exposed (NFR-01).
 */
public record ReservationDetailResponse(
        UUID id,
        Long serviceId,
        Integer qty,
        BigDecimal unitPrice,
        BigDecimal subtotal) {
}
