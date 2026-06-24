package com.neversion.api.reservation.infrastructure.adapters.in.rest.dto;

import java.util.UUID;

import com.neversion.api.account.domain.model.enums.SaleMode;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservationItemRequest(

        @NotNull UUID serviceUuid,
        @NotNull @Positive Integer qty,
        /** Sale mode for this item. Defaults to BY_PROFILE if omitted for backward compatibility. */
        SaleMode saleMode
) {
}
