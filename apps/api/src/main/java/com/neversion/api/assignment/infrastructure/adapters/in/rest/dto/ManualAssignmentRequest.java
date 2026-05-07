package com.neversion.api.assignment.infrastructure.adapters.in.rest.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ManualAssignmentRequest(
        @NotNull UUID clientId,
        @NotNull UUID serviceId,
        @NotNull UUID profileId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate) {
}
