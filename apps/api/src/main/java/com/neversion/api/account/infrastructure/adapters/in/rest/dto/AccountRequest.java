package com.neversion.api.account.infrastructure.adapters.in.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.neversion.api.account.domain.model.enums.SaleMode;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * Request body for creating or updating a master account (US-022 / US-023).
 * vendorId is NOT accepted here — resolved from the JWT caller (ADR-09 / EPIC-02 pattern).
 */
@Builder
public record AccountRequest(
        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,

        @NotBlank(message = "Password is required") String pass,

        @NotNull(message = "Service ID is required") Long serviceId,

        @NotNull(message = "Sale mode is required") SaleMode saleMode,

        @NotNull(message = "Renewal date is required") LocalDate renewalDate,

        /** Quality tier, e.g. "Familiar", "4K Ultra HD". Optional. */
        String plan,

        /** Acquisition cost paid to the wholesaler (US-022). Optional. */
        BigDecimal cost,

        /** Source/supplier where this account was purchased. Optional. */
        String source,

        /** Date the account was purchased from the wholesaler. Optional. */
        LocalDate purchasedAt,

        /** Private admin notes. Optional. */
        String notes) {
}
