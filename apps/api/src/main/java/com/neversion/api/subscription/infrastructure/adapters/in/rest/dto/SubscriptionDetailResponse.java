package com.neversion.api.subscription.infrastructure.adapters.in.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.order.domain.model.enums.OrderStatus;
import com.neversion.api.profile.domain.model.enums.ProfileStatus;
import com.neversion.api.shared.domain.model.enums.AccountStatus;
import com.neversion.api.subscription.domain.model.enums.SubStatus;

import lombok.Builder;

@Builder
public record SubscriptionDetailResponse(
        UUID id,
        SubStatus status,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate paymentDueDate,
        Long monthsPaid,
        String notes,
        LocalDateTime createdAt,
        FinancialSnapshot financialSnapshot,
        ClientSummary client,
        ProfileSummary profile,
        AccountSummary account,
        AccessSummary access,
        OrderSummary order) {

    public record FinancialSnapshot(
            UUID serviceId,
            String serviceName,
            BigDecimal priceSold,
            BigDecimal discountApplied,
            SaleMode saleMode) {
    }

    public record ClientSummary(
            UUID id,
            String name,
            String email,
            String phone) {
    }

    public record ProfileSummary(
            UUID id,
            String name,
            String pin,
            /** Operational notes: invitation link or personal email for Spotify Family slots. */
            String notes,
            Boolean isOwner,
            ProfileStatus status) {
    }

    public record AccountSummary(
            UUID id,
            String email,
            String plan,
            SaleMode saleMode,
            AccountStatus status) {
    }

    public record AccessSummary(
            String accountEmail,
            String accountPassword,
            String profileName,
            String profilePin,
            SaleMode saleMode) {
    }

    public record OrderSummary(
            UUID id,
            UUID reservationId,
            OrderStatus status,
            String paymentMethod,
            BigDecimal total,
            BigDecimal discount,
            String receiptUrl,
            Instant approvedAt,
            Instant createdAt) {
    }
}
