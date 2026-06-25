package com.neversion.api.subscription.infrastructure.adapters.in.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BatchCreateManualSubscriptionRequest(
        @NotNull UUID clientId,
        @NotEmpty @Size(max = 20) @Valid List<BatchItem> items,
        @NotNull @DecimalMin("0.00") BigDecimal discountApplied,
        @NotNull LocalDate paymentDueDate,
        String notes,
        boolean sendNotification) {

    public record BatchItem(
            @NotNull UUID serviceId,
            @NotNull @Min(1) Integer quantity,
            @NotNull @DecimalMin("0.00") BigDecimal priceSold,
            UUID profileId) {
    }
}
