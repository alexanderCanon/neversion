package com.neversion.api.reservation.infrastructure.adapters.in.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.neversion.api.reservation.domain.model.enums.ReservationStatus;

public record ReservationResponse(
                UUID id,
                UUID clientId,
                ReservationStatus status,
                BigDecimal discount,
                BigDecimal total,
                String receiptUrl,
                Instant expirationDate,
                Instant createdAt,
                List<ReservationDetailResponse> details) {
}
