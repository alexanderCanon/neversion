package com.neversion.api.reservation.application.port.in;

import java.util.UUID;

import com.neversion.api.reservation.domain.model.Reservation;

/**
 * Cancels a reservation. Only PENDING or UPLOADED reservations may be cancelled.
 * BR-US033: Admin or customer cancels before validation occurs.
 */
public interface CancelReservationUseCase {

    Reservation cancel(UUID reservationId);
}
