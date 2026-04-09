package com.neversion.api.account.infrastructure.adapters.in.rest.dto;

import java.time.LocalDate;

import com.neversion.api.account.domain.model.enums.SaleMode;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AccountRequest(
        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,

        @NotBlank(message = "Password is required") String pass,

        @NotNull(message = "Service ID is required") Long serviceId,

        @NotNull(message = "Sale mode is required") SaleMode saleMode,

        @NotNull(message = "Renewal date is required") LocalDate renewalDate,

        String notes) {
}
