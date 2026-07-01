package com.neversion.api.reservation.infrastructure.adapters.in.rest.dto;

import java.util.List;
import java.util.UUID;

import com.neversion.api.shared.domain.model.enums.AccountPreference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for creating a reservation (US-033).
 * EPIC-05: Added paymentMethod (selected by client at checkout).
 * Spotify: Added notes for account preference (cuenta nueva / cuenta propia).
 */
public record ReservationRequest(

        @NotNull UUID clientId,
        @NotNull @NotEmpty List<@Valid ReservationItemRequest> items,
        String paymentMethod,
        AccountPreference accountPreference,
        /** Client notes, e.g. Spotify account preference captured at checkout. */
        String notes,
        /** Loyalty points to redeem as a discount at checkout (1 point = 1 GTQ). Optional. */
        Long pointsToRedeem) {
}
