package com.neversion.api.reservation.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservationItemRequest(

        @NotNull Long inventoryId,
        @NotNull @Positive Integer qty) {
}
