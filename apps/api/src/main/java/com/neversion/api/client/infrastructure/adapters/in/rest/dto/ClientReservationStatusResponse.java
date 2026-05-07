package com.neversion.api.client.infrastructure.adapters.in.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Reservation and receipt status visible to the authenticated client.")
public record ClientReservationStatusResponse(
        UUID id,
        String status,
        BigDecimal total,
        BigDecimal discount,
        String receiptUrl,
        String paymentMethod,
        Instant expirationDate,
        Instant createdAt,
        String notes,
        UUID renewalSubscriptionId,
        List<ClientOrderServiceResponse> services) {
}
