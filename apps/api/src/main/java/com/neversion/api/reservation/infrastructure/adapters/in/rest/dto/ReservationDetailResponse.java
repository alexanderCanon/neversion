package com.neversion.api.reservation.infrastructure.adapters.in.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ReservationDetailResponse(
        UUID uuid,
        Long serviceId,
        Integer qty,
        BigDecimal unitPrice,
        BigDecimal subtotal) {
}
