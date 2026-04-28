package com.neversion.api.reservation.application.port.in;

import java.util.UUID;

import com.neversion.api.reservation.domain.model.Reservation;

/**
 * UC3: Validate and Formalize Sale — admin validates payment.
 */
public interface ValidateReservationUseCase {

    Reservation validate(UUID reservationId, String notes, String callerExternalId);
}
