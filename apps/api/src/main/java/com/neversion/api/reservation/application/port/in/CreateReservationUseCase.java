package com.neversion.api.reservation.application.port.in;

import java.util.List;
import java.util.UUID;

import com.neversion.api.reservation.domain.model.Reservation;

import com.neversion.api.shared.domain.model.enums.AccountPreference;

/**
 * UC1: Create Reservation (Checkout) — US-033.
 * Creates a reservation linking a client to selected services in the store.
 */
public interface CreateReservationUseCase {

    /**
     * Creates a new reservation with items from the storefront.
     *
     * @param clientUuid     UUID of the client placing the reservation
     * @param items          list of services + quantities
     * @param paymentMethod  payment method selected by the client (BR-06)
     * @param accountPreference optional client account preference for Spotify Fam
     * @param notes          optional client notes, e.g. Spotify account preference
     * @param pointsToRedeem optional loyalty points to redeem as a discount (1 point = 1 GTQ), null or 0 to skip
     * @return the persisted reservation with pricing and details
     */
    Reservation create(UUID clientUuid, List<ReservationItemCommand> items, String paymentMethod,
            AccountPreference accountPreference, String notes, Long pointsToRedeem);
}
