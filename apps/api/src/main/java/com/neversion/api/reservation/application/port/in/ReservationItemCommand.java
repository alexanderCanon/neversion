package com.neversion.api.reservation.application.port.in;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservationItemCommand(
        @NotNull UUID serviceUuid,
        @NotNull @Positive Integer qty) {
}
