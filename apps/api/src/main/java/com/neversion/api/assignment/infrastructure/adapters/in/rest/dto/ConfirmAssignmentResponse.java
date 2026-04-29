package com.neversion.api.assignment.infrastructure.adapters.in.rest.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ConfirmAssignmentResponse(
        UUID subscriptionId,
        UUID orderId,
        UUID profileId,
        UUID clientId,
        String serviceName,
        LocalDate startDate,
        LocalDate endDate,
        boolean notificationQueued) {
}
