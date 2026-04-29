package com.neversion.api.assignment.application.port.in;

import java.util.UUID;

import com.neversion.api.assignment.application.port.in.dto.AssignmentSuggestion;

public interface SuggestAssignmentUseCase {
    AssignmentSuggestion suggest(UUID orderUuid, String callerExternalId);
}
