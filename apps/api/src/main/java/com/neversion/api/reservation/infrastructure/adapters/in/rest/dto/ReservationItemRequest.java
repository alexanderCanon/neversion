package com.neversion.api.reservation.infrastructure.adapters.in.rest.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservationItemRequest(

        @NotNull UUID serviceUuid,
        @NotNull @Positive Integer qty) {
}
