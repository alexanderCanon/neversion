package com.neversion.api.reservation.application.port.in;

import java.util.UUID;

import com.neversion.api.reservation.domain.model.Reservation;

/**
 * UC4: Reject Receipt — US-036.
 * The vendor rejects the payment receipt and provides a reason.
 */
public interface RejectReservationUseCase {

    Reservation reject(UUID reservationId, String reason, String callerExternalId);
}
