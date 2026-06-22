package com.neversion.api.reservation.application.port.in;

import java.util.List;
import java.util.UUID;

import com.neversion.api.reservation.domain.model.Reservation;

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
     * @param notes          optional client notes, e.g. Spotify account preference
     * @return the persisted reservation with pricing and details
     */
    Reservation create(UUID clientUuid, List<ReservationItemCommand> items, String paymentMethod, String notes);
}
