package com.neversion.api.assignment.infrastructure.adapters.in.rest.mapper;

import org.springframework.stereotype.Component;

import com.neversion.api.assignment.application.port.in.dto.AssignmentResult;
import com.neversion.api.assignment.application.port.in.dto.AssignmentSuggestion;
import com.neversion.api.assignment.infrastructure.adapters.in.rest.dto.ConfirmAssignmentResponse;
import com.neversion.api.assignment.infrastructure.adapters.in.rest.dto.ManualAssignmentResponse;
import com.neversion.api.assignment.infrastructure.adapters.in.rest.dto.SuggestAssignmentResponse;

@Component
public class AssignmentRestMapper {

    public SuggestAssignmentResponse toResponse(AssignmentSuggestion suggestion) {
        return new SuggestAssignmentResponse(
                suggestion.hasSuggestion(),
                suggestion.saleMode(),
                suggestion.suggestedProfileUuid(),
                suggestion.suggestedAccountUuid(),
                suggestion.serviceName(),
                suggestion.accountEmail(),
                suggestion.noInventoryReason());
    }

    public ConfirmAssignmentResponse toConfirmResponse(AssignmentResult result) {
        return new ConfirmAssignmentResponse(
                result.subscriptionUuid(),
                result.orderUuid(),
                result.profileUuid(),
                result.clientUuid(),
                result.serviceName(),
                result.startDate(),
                result.endDate(),
                result.notificationQueued());
    }

    public ManualAssignmentResponse toManualResponse(AssignmentResult result) {
        return new ManualAssignmentResponse(
                result.subscriptionUuid(),
                result.orderUuid(),
                result.profileUuid(),
                result.clientUuid(),
                result.serviceName(),
                result.startDate(),
                result.endDate(),
                result.notificationQueued());
    }
}
