package com.neversion.api.reservation.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Line item in a reservation.
 * US-010: PK UUID→Long, inventoryId→serviceId.
 */
public record ReservationDetail(
        Long id,
        UUID uuid,
        Long reservationId,
        Long serviceId,
        Integer qty,
        BigDecimal unitPrice,
        BigDecimal subtotal) {
}
