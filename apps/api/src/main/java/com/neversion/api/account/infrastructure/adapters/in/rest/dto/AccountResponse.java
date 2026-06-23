package com.neversion.api.account.infrastructure.adapters.in.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.neversion.api.account.domain.model.enums.ProfileDeliveryType;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.shared.domain.model.enums.AccountStatus;

import lombok.Builder;

/**
 * Response for any account operation (US-022 .. US-028).
 * Includes status in all responses per operational consistency (confirmed).
 * Profile counters (US-024): populated in list views; zero-valued in create/update responses.
 */
@Builder
public record AccountResponse(
        UUID id,
        String email,
        String password,
        Long serviceId,
        UUID serviceUuid,
        String serviceName,
        SaleMode saleMode,
        ProfileDeliveryType profileDeliveryType,
        AccountStatus status,
        LocalDate renewalDate,
        String plan,
        BigDecimal cost,
        String source,
        LocalDate purchasedAt,
        String notes,
        LocalDateTime createdAt,

        /** Max profiles this account can hold. */
        Integer maxProfiles,

        /** US-024 — profile counters for listing view. */
        int totalProfiles,
        int availableProfiles,
        int occupiedProfiles,
        int blockedProfiles) {
}
