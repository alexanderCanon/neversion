package com.neversion.api.assignment.application.port.in.dto;

import java.util.UUID;

import com.neversion.api.account.domain.model.enums.SaleMode;

public record AssignmentSuggestion(
        boolean hasSuggestion,
        SaleMode saleMode,
        UUID suggestedProfileUuid,
        UUID suggestedAccountUuid,
        String serviceName,
        String accountEmail,
        String noInventoryReason) {
}
