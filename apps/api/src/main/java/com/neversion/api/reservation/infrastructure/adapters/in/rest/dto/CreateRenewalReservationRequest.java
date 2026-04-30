package com.neversion.api.reservation.infrastructure.adapters.in.rest.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for creating a renewal reservation (EPIC-09 / US-061).
 */
public record CreateRenewalReservationRequest(
        @NotNull UUID subscriptionId,
        String paymentMethod) {
}

