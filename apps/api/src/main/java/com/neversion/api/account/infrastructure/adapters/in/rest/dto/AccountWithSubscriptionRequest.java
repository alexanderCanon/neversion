package com.neversion.api.account.infrastructure.adapters.in.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountWithSubscriptionRequest(
        // ── Account fields ──
        @NotBlank String email,
        @NotBlank String password,
        @NotNull UUID serviceUuid,
        @NotBlank String saleMode,
        @NotNull LocalDate renewalDate,
        String plan,
        BigDecimal cost,
        String source,
        LocalDate purchasedAt,
        String accountNotes,
        Integer maxProfiles,
        // ── Subscription fields ──
        @NotNull UUID clientUuid,
        @NotNull LocalDate paymentDueDate,
        BigDecimal priceSold,
        BigDecimal discountApplied,
        String subscriptionNotes,
        boolean sendNotification) {
}
