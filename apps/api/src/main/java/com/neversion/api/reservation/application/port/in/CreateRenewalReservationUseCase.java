package com.neversion.api.reservation.application.port.in;

import java.util.UUID;

import com.neversion.api.reservation.domain.model.Reservation;

/**
 * Creates a reservation that represents a client-initiated subscription renewal.
 */
public interface CreateRenewalReservationUseCase {

    Reservation create(UUID subscriptionUuid, String paymentMethod, String callerExternalId);
}

