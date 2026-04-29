package com.neversion.api.assignment.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

import com.neversion.api.assignment.application.port.in.dto.AssignmentResult;

public interface ManualAssignmentUseCase {
    AssignmentResult assign(UUID clientUuid, UUID serviceUuid, UUID profileUuid,
            LocalDate startDate, LocalDate endDate, String callerExternalId);
}
