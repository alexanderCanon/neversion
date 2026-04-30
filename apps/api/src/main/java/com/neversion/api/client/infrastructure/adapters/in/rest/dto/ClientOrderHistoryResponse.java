package com.neversion.api.client.infrastructure.adapters.in.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Order history row for the authenticated client.")
public record ClientOrderHistoryResponse(
        UUID id,
        UUID reservationId,
        String status,
        String paymentMethod,
        BigDecimal total,
        BigDecimal discount,
        String receiptUrl,
        Instant approvedAt,
        Instant createdAt,
        List<ClientOrderServiceResponse> services) {
}
