package com.neversion.api.reservation.infrastructure.adapters.in.rest.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ReservationRequest(

        UUID clientId,
        @NotNull @NotEmpty List<@Valid ReservationItemRequest> items) {
}
