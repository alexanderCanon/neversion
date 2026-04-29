package com.neversion.api.assignment.infrastructure.adapters.in.rest.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ConfirmAssignmentRequest(
        @NotNull UUID profileId) {
}
