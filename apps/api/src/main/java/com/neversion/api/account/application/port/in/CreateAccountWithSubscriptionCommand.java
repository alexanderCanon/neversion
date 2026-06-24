package com.neversion.api.account.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Command for creating an account and immediately assigning a subscription
 * to an existing client in a single transaction.
 */
public record CreateAccountWithSubscriptionCommand(
        // ── Account fields ──
        String email,
        String password,
        UUID serviceUuid,
        String saleMode,
        LocalDate renewalDate,
        String plan,
        BigDecimal cost,
        String source,
        LocalDate purchasedAt,
        String accountNotes,
        Integer maxProfiles,
        // ── Subscription fields ──
        UUID clientUuid,
        LocalDate paymentDueDate,
        BigDecimal priceSold,
        BigDecimal discountApplied,
        String subscriptionNotes,
        boolean sendNotification) {
}
