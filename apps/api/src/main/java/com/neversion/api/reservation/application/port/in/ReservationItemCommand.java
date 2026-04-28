package com.neversion.api.reservation.application.port.in;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservationItemCommand(
        @NotNull Long serviceId,
        @NotNull @Positive Integer qty) {
}
