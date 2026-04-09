package com.neversion.api.reservation.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record ReservationDetail(
                UUID id,
                UUID reservationId,
                Long inventoryId,
                Integer qty,
                BigDecimal unitPrice,
                BigDecimal subtotal) {
}
