package com.neversion.api.assignment.application.port.in.dto;

import java.time.LocalDate;
import java.util.UUID;

public record AssignmentResult(
        UUID subscriptionUuid,
        UUID orderUuid,
        UUID profileUuid,
        UUID clientUuid,
        String serviceName,
        LocalDate startDate,
        LocalDate endDate,
        boolean notificationQueued) {
}
