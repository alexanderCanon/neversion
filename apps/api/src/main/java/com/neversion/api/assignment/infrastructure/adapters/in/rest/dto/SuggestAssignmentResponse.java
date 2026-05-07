package com.neversion.api.assignment.infrastructure.adapters.in.rest.dto;

import java.util.UUID;

import com.neversion.api.account.domain.model.enums.SaleMode;

public record SuggestAssignmentResponse(
        boolean hasSuggestion,
        SaleMode saleMode,
        UUID suggestedProfileId,
        UUID suggestedAccountId,
        String serviceName,
        String accountEmail,
        String noInventoryReason) {
}
