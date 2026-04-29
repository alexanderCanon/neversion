package com.neversion.api.assignment.application.port.in;

import java.util.UUID;

import com.neversion.api.assignment.application.port.in.dto.AssignmentResult;

public interface ConfirmAssignmentUseCase {
    AssignmentResult confirm(UUID orderUuid, UUID profileUuid, String callerExternalId);
}
