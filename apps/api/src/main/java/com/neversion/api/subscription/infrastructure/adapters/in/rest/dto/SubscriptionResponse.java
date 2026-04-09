package com.neversion.api.subscription.infrastructure.adapters.in.rest.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.neversion.api.subscription.domain.model.enums.SubStatus;

import lombok.Builder;

@Builder
public record SubscriptionResponse(
        UUID id,
        UUID profileId,
        UUID clientId,
        UUID accountId,
        SubStatus status,
        LocalDate startDate,
        LocalDate paymentDueDate,
        Long monthsPaid,
        String notes,
        LocalDateTime createdAt) {
}
