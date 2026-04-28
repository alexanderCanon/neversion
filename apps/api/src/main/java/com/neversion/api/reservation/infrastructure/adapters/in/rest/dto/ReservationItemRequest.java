package com.neversion.api.reservation.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservationItemRequest(

        @NotNull Long serviceId,
        @NotNull @Positive Integer qty) {
}
