package com.neversion.api.reservation.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.reservation.application.port.in.CancelReservationUseCase;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.domain.port.out.ReservationRepositoryPort;

/**
 * Cancels a reservation (PENDING or UPLOADED → CANCELLED).
 * Business rule: finalized reservations (VALIDATED, REJECTED, CANCELLED) cannot be cancelled.
 * Extracted from ReservationController to honour hexagonal architecture boundaries.
 */
@Service
public class CancelReservationService implements CancelReservationUseCase {

    private final ReservationRepositoryPort reservationRepositoryPort;

    public CancelReservationService(ReservationRepositoryPort reservationRepositoryPort) {
        this.reservationRepositoryPort = reservationRepositoryPort;
    }

    @Override
    @Transactional
    public Reservation cancel(UUID reservationId) {
        Reservation reservation = reservationRepositoryPort.findByUuid(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found with id: " + reservationId));

        // BR: only non-finalized reservations may be cancelled
        if (reservation.getStatus() != ReservationStatus.PENDING
                && reservation.getStatus() != ReservationStatus.UPLOADED) {
            throw new BusinessRuleException(
                    "Only PENDING or UPLOADED reservations can be cancelled. Current status: "
                            + reservation.getStatus());
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepositoryPort.update(reservation);
    }
}
