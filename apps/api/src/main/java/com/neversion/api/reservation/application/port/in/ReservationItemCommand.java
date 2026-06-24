package com.neversion.api.reservation.application.port.in;

import java.util.UUID;

import com.neversion.api.account.domain.model.enums.SaleMode;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservationItemCommand(
        @NotNull UUID serviceUuid,
        @NotNull @Positive Integer qty,
        /** Sale mode for this item. Defaults to BY_PROFILE if null for backward compatibility. */
        SaleMode saleMode
) {
}
