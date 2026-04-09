package com.neversion.api.account.infrastructure.adapters.in.rest.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.neversion.api.account.domain.model.enums.SaleMode;

import lombok.Builder;

@Builder
public record AccountResponse(
        UUID id,
        String email,
        String pass,
        Long serviceId,
        SaleMode saleMode,
        LocalDate renewalDate,
        String notes,
        LocalDateTime createdAt) {
}
