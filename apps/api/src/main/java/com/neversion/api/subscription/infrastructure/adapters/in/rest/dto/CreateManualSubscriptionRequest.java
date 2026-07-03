package com.neversion.api.subscription.infrastructure.adapters.in.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CreateManualSubscriptionRequest(
        @NotNull UUID clientId,
        @NotNull UUID profileId,
        @NotNull UUID serviceId,
        @NotNull @DecimalMin("0.00") BigDecimal priceSold,
        @DecimalMin("0.00") BigDecimal discountApplied,
        LocalDate startDate,
        @NotNull LocalDate paymentDueDate,
        String notes,
        boolean sendNotification) {
}
