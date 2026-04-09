package com.neversion.api.reservation.infrastructure.adapters.in.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ReservationDetailResponse(
        UUID id,
        Long inventoryId,
        Integer qty,
        BigDecimal unitPrice,
        BigDecimal subtotal) {
}
